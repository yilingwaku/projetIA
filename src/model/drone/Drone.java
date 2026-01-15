package model.drone;

import model.shared.Position;

import java.util.Random;

public class Drone {
    private final int id;
    private Position position;
    private DroneState state;

    // Un générateur aléatoire pour des déplacements simples
    private final Random rng;

    /**
     * États possibles d'un drone.
     * - ACTIVE : exploration
     * - ANALYZE : analyse approfondie pendant 10s (temps simulé)
     * - RETURNING : retour vers la base
     * - RECHARGING : recharge à la base pendant 10min (temps simulé)
     */
    public enum DroneState {
        ACTIVE,
        ANALYZE,
        RETURNING,
        RECHARGING
    }


    /**
     * @param id identifiant unique du drone
     * @param startPosition position initiale (base)
     * @param rng Random
     */
    public Drone(int id, Position startPosition, Random rng) {
        this.id = id;
        this.position = startPosition;
        this.state = DroneState.ACTIVE;
        this.rng = rng;
    }

    /** @return identifiant du drone */
    public int getId() {
        return id;
    }

    /** @return position courante */
    public Position getPosition() {
        return position;
    }

    /** @return état courant */
    public DroneState getState() {
        return state;
    }

    /**
     * Un step de simulation
     * Dans cette étape 1, on fait uniquement un déplacement simple si ACTIVE.
     * @param width largeur de la grille
     * @param height hauteur de la grille
     */
    public void step(int width, int height) {
        switch (state) {
            case ACTIVE -> moveRandomly(width, height);
            case ANALYZE, RETURNING, RECHARGING -> {
                // Pour l'instant, rien .
            }
        }
    }

    /**
     * Déplacement aléatoire dans le voisinage 4-directions.
     */
    private void moveRandomly(int width, int height) {
        // 4 directions: haut, bas, gauche, droite
        int[][] dirs = new int[][]{
                {0, -1}, // haut
                {0, 1},  // bas
                {-1, 0}, // gauche
                {1, 0}   // droite
        };

        // Essaye quelques fois pour éviter de sortir de la grille
        for (int attempts = 0; attempts < 8; attempts++) {
            int[] d = dirs[rng.nextInt(dirs.length)];
            Position candidate = position.translate(d[0], d[1]);

            if (candidate.getX() >= 0 && candidate.getX() < width
                    && candidate.getY() >= 0 && candidate.getY() < height) {
                position = candidate;
                return;
            }
        }
        // Si aucune direction ne marche,le drone reste sur place.
    }

    public void setState(DroneState newState) {
        this.state = newState;
    }
}
