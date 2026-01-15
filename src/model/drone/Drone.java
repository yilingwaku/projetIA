package model.drone;

import model.shared.Position;

import java.util.Random;

/**
 * Drone avec gestion du temps simulé en secondes.
 *
 * Convention :
 * - 1 step = 1 seconde simulée
 * - Analyse = 10s
 * - Autonomie max = 30min = 1800s
 * - Recharge = 10min = 600s
 *
 * IMPORTANT : le temps "réel" (Thread.sleep) sert uniquement à ralentir l'affichage,
 * il ne représente pas le temps simulé.
 */
public class Drone {

    private final int id;
    private Position position;
    private final Position basePosition;
    private DroneState state;

    /** Générateur aléatoire (temporaire, remplacé par ACO plus tard) */
    private final Random rng;

    // --- Paramètres (en secondes simulées) ---
    private static final int MAX_ACTIVE_TIME_SEC = 30 * 60; // 1800s
    private static final int RECHARGE_TIME_SEC = 10 * 60;   // 600s
    private static final int ANALYZE_TIME_SEC = 10;         // 10s

    // Marge de sécurité pour garantir le retour (en secondes simulées)
    private static final int SAFETY_MARGIN_SEC = 30;

    // --- Compteurs (en secondes simulées) ---
    private int activeElapsedSec;        // temps consommé depuis dernière recharge (inclut retour/analyse)
    private int rechargeRemainingSec;    // temps restant de recharge
    private int analyzeRemainingSec;     // temps restant d'analyse

    /**
     * États possibles d'un drone.
     * - ACTIVE : exploration
     * - ANALYZE : analyse approfondie (10s)
     * - RETURNING : retour vers la base
     * - RECHARGING : recharge à la base (10min)
     */
    public enum DroneState {
        ACTIVE,
        ANALYZE,
        RETURNING,
        RECHARGING
    }

    /**
     * @param id identifiant unique du drone
     * @param startPosition position initiale (souvent la base)
     * @param basePosition position de la base
     * @param rng générateur aléatoire
     */
    public Drone(int id, Position startPosition, Position basePosition, Random rng) {
        this.id = id;
        this.position = startPosition;
        this.basePosition = basePosition;
        this.state = DroneState.ACTIVE;
        this.rng = rng;

        this.activeElapsedSec = 0;
        this.rechargeRemainingSec = 0;
        this.analyzeRemainingSec = 0;
    }

    public int getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public DroneState getState() {
        return state;
    }

    // =========================
    // Temps & énergie (helpers)
    // =========================

    /** @return temps actif restant avant épuisement (en secondes simulées) */
    public int getRemainingActiveSec() {
        return Math.max(0, MAX_ACTIVE_TIME_SEC - activeElapsedSec);
    }

    /** @return temps de recharge restant (en secondes simulées) */
    public int getRechargeRemainingSec() {
        return rechargeRemainingSec;
    }

    /** @return temps d'analyse restant (en secondes simulées) */
    public int getAnalyzeRemainingSec() {
        return analyzeRemainingSec;
    }

    /**
     * Distance Manhattan jusqu'à la base (en nombre de cases).
     * En RETURNING, on se déplace de 1 case par step => 1 case ~= 1 seconde simulée.
     */
    private int manhattanToBaseCells() {
        return Math.abs(position.getX() - basePosition.getX())
                + Math.abs(position.getY() - basePosition.getY());
    }

    /**
     * Estimation du temps minimal de retour (en secondes simulées).
     * Hypothèse : 1 case par seconde simulée.
     */
    private int estimatedReturnTimeSec() {
        return manhattanToBaseCells();
    }

    // =========================
    // Simulation step
    // =========================

    /**
     * Un step de simulation (1 seconde simulée).
     * @param width largeur de la grille
     * @param height hauteur de la grille
     */
    public void step(int width, int height) {
        switch (state) {
            case ACTIVE -> stepActive(width, height);
            case ANALYZE -> stepAnalyze();
            case RETURNING -> stepReturning(width, height);
            case RECHARGING -> stepRecharging();
        }
    }

    /**
     * ACTIVE :
     * - déplacement (aléatoire temporaire)
     * - consommation d'énergie
     * - possibilité de déclencher une analyse (placeholder)
     * - déclenchement du retour anticipé si nécessaire
     */
    private void stepActive(int width, int height) {
        moveRandomly(width, height);

        // Consommation d'énergie : 1s par step
        activeElapsedSec += 1;

        // (Placeholder) : détection d'anomalie -> analyse
        if (detectAnomalyPlaceholder()) {
            state = DroneState.ANALYZE;
            analyzeRemainingSec = ANALYZE_TIME_SEC;
            return;
        }

        // Retour anticipé : si temps restant insuffisant pour rentrer + marge
        int remaining = getRemainingActiveSec();
        int returnTime = estimatedReturnTimeSec();
        if (remaining <= returnTime + SAFETY_MARGIN_SEC) {
            state = DroneState.RETURNING;
        }
    }

    /**
     * ANALYZE :
     * - le drone reste sur place
     * - consomme du temps/énergie
     * - dure 10 secondes simulées
     * - à la fin : retourne en ACTIVE ou RETURNING selon l'énergie restante
     */
    private void stepAnalyze() {
        // Analyse consomme aussi de l'énergie
        activeElapsedSec += 1;

        analyzeRemainingSec -= 1;
        if (analyzeRemainingSec <= 0) {
            analyzeRemainingSec = 0;

            int remaining = getRemainingActiveSec();
            int returnTime = estimatedReturnTimeSec();

            if (remaining <= returnTime + SAFETY_MARGIN_SEC) {
                state = DroneState.RETURNING;
            } else {
                state = DroneState.ACTIVE;
            }
        }
    }

    /**
     * RETURNING :
     * - déplacement déterministe vers la base (réduit la distance)
     * - consomme du temps/énergie
     * - arrivé à la base => RECHARGING
     */
    private void stepReturning(int width, int height) {
        // Si déjà à la base : commence la recharge
        if (position.equals(basePosition)) {
            state = DroneState.RECHARGING;
            rechargeRemainingSec = RECHARGE_TIME_SEC;
            return;
        }

        int dx = Integer.compare(basePosition.getX(), position.getX()); // -1, 0, 1
        int dy = Integer.compare(basePosition.getY(), position.getY()); // -1, 0, 1

        // Politique simple : priorité à X, sinon Y
        Position candidateX = position.translate(dx, 0);
        Position candidateY = position.translate(0, dy);

        if (dx != 0 && isInside(candidateX, width, height)) {
            position = candidateX;
        } else if (dy != 0 && isInside(candidateY, width, height)) {
            position = candidateY;
        }
        // Sinon : reste sur place (cas rare)

        // Le retour consomme de l'énergie
        activeElapsedSec += 1;

        // Arrivée à la base -> recharge
        if (position.equals(basePosition)) {
            state = DroneState.RECHARGING;
            rechargeRemainingSec = RECHARGE_TIME_SEC;
        }
    }

    /**
     * RECHARGING :
     * - recharge pendant 10 minutes (600s)
     * - fin recharge : reset énergie + retour en ACTIVE
     */
    private void stepRecharging() {
        rechargeRemainingSec -= 1;
        if (rechargeRemainingSec <= 0) {
            rechargeRemainingSec = 0;
            activeElapsedSec = 0;
            state = DroneState.ACTIVE;
        }
    }

    // =========================
    // Mouvement & détection (temporaire)
    // =========================

    /**
     * Déplacement aléatoire 4-directions (temporaire).
     * À remplacer par ACO.
     */
    private void moveRandomly(int width, int height) {
        int[][] dirs = new int[][]{
                {0, -1}, // haut
                {0, 1},  // bas
                {-1, 0}, // gauche
                {1, 0}   // droite
        };

        for (int attempts = 0; attempts < 8; attempts++) {
            int[] d = dirs[rng.nextInt(dirs.length)];
            Position candidate = position.translate(d[0], d[1]);

            if (isInside(candidate, width, height)) {
                position = candidate;
                return;
            }
        }
        // Si aucune direction ne marche, le drone reste sur place.
    }

    private boolean isInside(Position p, int width, int height) {
        return p.getX() >= 0 && p.getX() < width
                && p.getY() >= 0 && p.getY() < height;
    }

    /**
     * Détection placeholder.
     * À remplacer par un capteur réel sur la Map.
     */
    private boolean detectAnomalyPlaceholder() {
        // 2% de chances par seconde simulée (à ajuster)
        return rng.nextDouble() < 0.02;
    }

    public void setState(DroneState newState) {
        this.state = newState;
    }
}
