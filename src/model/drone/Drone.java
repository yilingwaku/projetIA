package model.drone;

import model.environment.Map;
import model.shared.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Drone final :
 * - 1 step = 1 seconde simulée
 * - ACO minimal : choix probabiliste basé sur tauLocal
 * - Upload anytime / Download only at base : tauLocal synchronisé uniquement à la base
 * - Exploration de catastrophe : observe Map.CaseType sur sa cellule, déclenche ANALYZE si anomalie
 */
public class Drone {

    private final int id;
    private Position position;
    private final Position basePosition;
    private DroneState state;

    private final Random rng;

    // Temps (secondes)
    private static final int MAX_ACTIVE_TIME_SEC = 30 * 60; // 1800
    private static final int RECHARGE_TIME_SEC = 10 * 60;   // 600
    private static final int ANALYZE_TIME_SEC = 10;         // 10
    private static final int SAFETY_MARGIN_SEC = 30;

    private int activeElapsedSec;
    private int rechargeRemainingSec;
    private int analyzeRemainingSec;

    // ACO minimal : phéromones locales
    private final double[][] tauLocal;
    private static final double INITIAL_TAU = 1.0;

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
    }

    public int getId() { return id; }

    public Position getPosition() { return position; }

    public DroneState getState() { return state; }

    // ===== Communication (download only at base) =====

    public void syncTau(double[][] globalTau) {
        for (int x = 0; x < tauLocal.length; x++) {
            System.arraycopy(globalTau[x], 0, tauLocal[x], 0, tauLocal[0].length);
        }
    }

    // ===== Observation locale (capteur) =====

    /**
     * Appelé par la simulation : le drone observe uniquement sa cellule.
     * Si anomalie => déclenche ANALYZE (10s).
     */
    public void observe(Map.CaseType observed) {
        if (state == DroneState.ACTIVE) {
            if (observed == Map.CaseType.Pollution
                    || observed == Map.CaseType.Collapse
                    || observed == Map.CaseType.RestrictedArea) {

                state = DroneState.ANALYZE;
                analyzeRemainingSec = ANALYZE_TIME_SEC;
            }
        }
    }

    // ===== Step =====

    public void step(int width, int height) {
        switch (state) {
            case ACTIVE -> stepActive(width, height);
            case ANALYZE -> stepAnalyze();
            case RETURNING -> stepReturning(width, height);
            case RECHARGING -> stepRecharging();
        }
    }

    private void stepActive(int width, int height) {
        position = chooseNextMoveACO(width, height);
        activeElapsedSec++;

        // Vérification retour anticipé
        if (getRemainingActiveSec() <= estimatedReturnTimeSec() + SAFETY_MARGIN_SEC) {
            state = DroneState.RETURNING;
        }
    }

    private void stepAnalyze() {
        activeElapsedSec++;
        analyzeRemainingSec--;

        if (analyzeRemainingSec <= 0) {
            analyzeRemainingSec = 0;
            if (getRemainingActiveSec() <= estimatedReturnTimeSec() + SAFETY_MARGIN_SEC) {
                state = DroneState.RETURNING;
            } else {
                state = DroneState.ACTIVE;
            }
        }
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
            position = nextX;
        } else if (dy != 0 && isInside(nextY, width, height)) {
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

    // ===== ACO minimal =====

    private Position chooseNextMoveACO(int width, int height) {
        int x = position.getX();
        int y = position.getY();

        List<Position> neighbors = new ArrayList<>();
        if (x > 0) neighbors.add(new Position(x - 1, y));
        if (x < width - 1) neighbors.add(new Position(x + 1, y));
        if (y > 0) neighbors.add(new Position(x, y - 1));
        if (y < height - 1) neighbors.add(new Position(x, y + 1));

        double sum = 0.0;
        for (Position p : neighbors) {
            sum += tauLocal[p.getX()][p.getY()];
        }

        double r = rng.nextDouble() * sum;
        double acc = 0.0;

        for (Position p : neighbors) {
            acc += tauLocal[p.getX()][p.getY()];
            if (acc >= r) return p;
        }

        return neighbors.get(0);
    }

    // ===== Helpers =====

    private int getRemainingActiveSec() {
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
