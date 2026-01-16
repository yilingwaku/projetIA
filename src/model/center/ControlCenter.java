package model.center;

import model.environment.Map;

/**
 * Centre de contrôle – version finale minimale.
 *
 * - Maintient une matrice globale de phéromones (tau)
 * - Upload anytime : reçoit (x,y,type) à chaque step
 * - Download only at base : fournit tau uniquement quand le drone est à la base
 */
public class ControlCenter {

    private final int width;
    private final int height;
    private final double[][] tau;

    private static final double INITIAL_TAU = 1.0;
    private static final double RHO = 0.10; // évaporation

    public ControlCenter(int width, int height) {
        this.width = width;
        this.height = height;
        this.tau = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tau[x][y] = INITIAL_TAU;
            }
        }
    }

    /**
     * le drone signale ce qu'il observe.
     * On dépose plus de phéromone si c'est une anomalie.
     */
    public void reportCell(int x, int y, Map.CaseType observed) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;

        double add = 1.0; // passage normal

        if (observed == Map.CaseType.Pollution || observed == Map.CaseType.Collapse) {
            add = 5.0; // anomalies "importantes"
        } else if (observed == Map.CaseType.RestrictedArea) {
            add = 2.0; // zone interdite (au choix)
        }

        tau[x][y] += add;
    }

    /** Évaporation globale (1 fois par step) */
    public void evaporate() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tau[x][y] *= (1.0 - RHO);
            }
        }
    }

    /**
     * Download only at base : copie de tau.
     * Appeler uniquement à la base.
     */
    public double[][] copyTau() {
        double[][] copy = new double[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(tau[x], 0, copy[x], 0, height);
        }
        return copy;
    }

    // La moyenne de pheromone
    public double averageTau() {
        double sum = 0.0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sum += tau[x][y];
            }
        }
        return sum / (width * height);
    }
}
