package model.drone;

import model.environment.Map;
import model.shared.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Drone :
 * 1 step = 1 seconde simulée
 * Formule ACO utilisée :
 *      P_ij = (tau_ij^ALPHA * eta_ij^BETA) / somme_k(tau_ik^ALPHA * eta_ik^BETA)
 *  Extensions pratiques :
 *      avec probabilité Q0 → exploitation (on choisit le meilleur voisin)
 *      sinon → exploration (tirage probabiliste)
 *    le drone ne reçoit la phéromone globale QUE lorsqu’il revient à la base
 */
public class Drone {

    private final int id;
    private Position position;
    private final Position basePosition;
    private Position lastPosition;
    private Position lastAnalyzedPos = null;
    private DroneState state;

    private static final double ALPHA = 4.0; // importance of pheromone
    private static final double BETA  = 0.3; // importance of heuristic

    private static final double Q0 = 0.8;

    private final Random rng;

    // Temps (secondes)
    private static final int MAX_ACTIVE_TIME_SEC = 30 * 60; // 1800
    private static final int RECHARGE_TIME_SEC = 60;   // 60 pour tester
    private static final int ANALYZE_TIME_SEC = 10;         // 10
    private static final int SAFETY_MARGIN_SEC = 25 * 60;

    private int activeElapsedSec;
    private int rechargeRemainingSec;
    private int analyzeRemainingSec;
    private int nbAnomaliesAnalysed = 0;

    // ACO pheromone
    private final double[][] tauLocal;
    private static final double INITIAL_TAU = 1.0;

    private final int[][] visits;

    public enum DroneState {
        ACTIVE,
        ANALYZE,
        RETURNING,
        RECHARGING
    }

    public Drone(int id,
                 Position startPosition,
                 Position basePosition,
                 Random rng,
                 int width,
                 int height) {

        this.id = id;
        this.position = startPosition;
        this.basePosition = basePosition;
        this.lastPosition = null;
        this.state = DroneState.ACTIVE;
        this.rng = rng;

        this.activeElapsedSec = 0;
        this.rechargeRemainingSec = 0;
        this.analyzeRemainingSec = 0;

        this.tauLocal = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tauLocal[x][y] = INITIAL_TAU;
            }
        }

        this.visits = new int[width][height];
        this.visits[startPosition.getX()][startPosition.getY()]++;
    }

    public int getId() { return id; }
    public Position getPosition() { return position; }
    public DroneState getState() { return state; }

    public int getAnalyzeRemainingSec() { return analyzeRemainingSec; }
    public int getRechargeRemainingSec() { return rechargeRemainingSec; }

    // Communication sync
    public void syncTau(double[][] globalTau) {
        for (int x = 0; x < tauLocal.length; x++) {
            System.arraycopy(globalTau[x], 0, tauLocal[x], 0, tauLocal[0].length);
        }
        for (int i = 0; i < visits.length; i++) {
            for (int j = 0; j < visits[0].length; j++) {
                visits[i][j] = 0;
            }
        }
        visits[position.getX()][position.getY()] = 1;
    }

    // Observation
    public void observe(Map.CaseType observed) {
        if (state != DroneState.ACTIVE) return;

        boolean isAnomaly =
                observed == Map.CaseType.Pollution
                        || observed == Map.CaseType.Collapse
                        || observed == Map.CaseType.RestrictedArea;

        if (!isAnomaly) return;

        if (position.equals(lastAnalyzedPos)) {
            return;
        }

        state = DroneState.ANALYZE;
        analyzeRemainingSec = ANALYZE_TIME_SEC;
        lastAnalyzedPos = position;
    }



    // Step
    public void step(int width, int height) {
        switch (state) {
            case ACTIVE -> stepActive(width, height);
            case ANALYZE -> stepAnalyze();
            case RETURNING -> stepReturning(width, height);
            case RECHARGING -> stepRecharging();
        }
    }

    private void stepActive(int width, int height) {
        Position next = chooseNextMoveACO(width, height);

        lastPosition = position;
        position = next;
        visits[position.getX()][position.getY()]++;
        activeElapsedSec++;

        if (getRemainingActiveSec() <= estimatedReturnTimeSec() + SAFETY_MARGIN_SEC) {
            state = DroneState.RETURNING;
        }
    }

    private void stepAnalyze() {
        activeElapsedSec++;
        analyzeRemainingSec--;
        if (analyzeRemainingSec <= 0) {
            analyzeRemainingSec = 0;
            nbAnomaliesAnalysed++;
            if (getRemainingActiveSec() <= estimatedReturnTimeSec() + SAFETY_MARGIN_SEC) {
                state = DroneState.RETURNING;
            } else {
                state = DroneState.ACTIVE;
            }
        }
    }

    public int getNbAnomaliesAnalysed(){
        return nbAnomaliesAnalysed;
    }

    private void stepReturning(int width, int height) {
        if (position.equals(basePosition)) {
            state = DroneState.RECHARGING;
            rechargeRemainingSec = RECHARGE_TIME_SEC;
            return;
        }

        int dx = Integer.compare(basePosition.getX(), position.getX());
        int dy = Integer.compare(basePosition.getY(), position.getY());

        Position nextX = position.translate(dx, 0);
        Position nextY = position.translate(0, dy);

        if (dx != 0 && isInside(nextX, width, height)) {
            lastPosition = position;
            position = nextX;
        } else if (dy != 0 && isInside(nextY, width, height)) {
            lastPosition = position;
            position = nextY;
        }

        activeElapsedSec++;

        if (position.equals(basePosition)) {
            state = DroneState.RECHARGING;
            rechargeRemainingSec = RECHARGE_TIME_SEC;
        }
    }

    private void stepRecharging() {
        rechargeRemainingSec--;
        if (rechargeRemainingSec <= 0) {
            rechargeRemainingSec = 0;
            activeElapsedSec = 0;
            state = DroneState.ACTIVE;
        }
    }

    // En utilisant nouvelle fonction heuristique eta
    private double heuristic(Position p) {
        int v = visits[p.getX()][p.getY()];
        return 1.0 / (1.0 + v);
    }


    private Position chooseNextMoveACO(int width, int height) {

        int x = position.getX();
        int y = position.getY();

        List<Position> neighbors = new ArrayList<>();

        // neighbors + tabu (do not immediately go back)
        Position p;
        if (x > 0) { p = new Position(x - 1, y); if (!p.equals(lastPosition)) neighbors.add(p); }
        if (x < width - 1) { p = new Position(x + 1, y); if (!p.equals(lastPosition)) neighbors.add(p); }
        if (y > 0) { p = new Position(x, y - 1); if (!p.equals(lastPosition)) neighbors.add(p); }
        if (y < height - 1) { p = new Position(x, y + 1); if (!p.equals(lastPosition)) neighbors.add(p); }

        // fallback (corner case)
        if (neighbors.isEmpty()) {
            if (x > 0) neighbors.add(new Position(x - 1, y));
            if (x < width - 1) neighbors.add(new Position(x + 1, y));
            if (y > 0) neighbors.add(new Position(x, y - 1));
            if (y < height - 1) neighbors.add(new Position(x, y + 1));
        }

        // w = tau^alpha * eta^beta
        double[] weights = new double[neighbors.size()];
        double sum = 0.0;

        int bestIdx = 0;
        double bestW = -1.0;

        for (int i = 0; i < neighbors.size(); i++) {
            Position nb = neighbors.get(i);

            double tau = tauLocal[nb.getX()][nb.getY()];
            double eta = heuristic(nb); // 1/(1+visits)

            double w = Math.pow(tau, ALPHA) * Math.pow(eta, BETA);
            weights[i] = w;
            sum += w;

            if (w > bestW) {
                bestW = w;
                bestIdx = i;
            }
        }

        // Si tous est 0, random
        if (sum <= 0.0) {
            return neighbors.get(rng.nextInt(neighbors.size()));
        }

        // Q0
        // q <= Q0 => exploitation
        // else    => exploration
        double q = rng.nextDouble();
        if (q <= Q0) {
            return neighbors.get(bestIdx);
        }

        double r = rng.nextDouble() * sum;
        double acc = 0.0;
        for (int i = 0; i < neighbors.size(); i++) {
            acc += weights[i];
            if (acc >= r) return neighbors.get(i);
        }

        return neighbors.get(neighbors.size() - 1);
    }

    public int getRemainingActiveSec() {
        return Math.max(0, MAX_ACTIVE_TIME_SEC - activeElapsedSec);
    }

    private int estimatedReturnTimeSec() {
        return Math.abs(position.getX() - basePosition.getX())
                + Math.abs(position.getY() - basePosition.getY());
    }

    private boolean isInside(Position p, int width, int height) {
        return p.getX() >= 0 && p.getX() < width
                && p.getY() >= 0 && p.getY() < height;
    }
}
