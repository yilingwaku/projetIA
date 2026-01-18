package model.center;

import model.environment.Map;

/**
 * Centre de contrôle
 * - tau[x][y] est un champ de phéromone
 * - Upload anytime: chaque drone signale (x,y,observed) à chaque step.
 * - Download only at base: un drone copie tout tau -> tauLocal uniquement quand il est à la base.:
 */
public class ControlCenter {

    private final int width;
    private final int height;
    private final double[][] tau;

    // Global pheromone parameters
    private static final double INITIAL_TAU = 1.0;

    // Evaporation rate (0 < RHO < 1).  */
    private static final double RHO = 0.0003;

    private static final double ANOMALY_CORE = 100.0;
    private static final double ANOMALY_SPREAD = 10.0;   // ~20% of core

    private static final double RESTRICTED_CORE = 0;
    private static final double RESTRICTED_SPREAD = 0;

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
     * Upload anytime : le drone signale ce qu'il observe sur sa cellule.
     * On dépose plus si anomalie, et on diffuse légèrement aux 4 voisins pour créer un gradient.
     */
    public void reportCell(int x, int y, Map.CaseType observed) {
        if (!inside(x, y)) return;

        // Ne pas déposer à la base
        if (observed == Map.CaseType.BASE) return;

        double core;
        double spread;

        if (observed == Map.CaseType.Pollution || observed == Map.CaseType.Collapse) {
            core = ANOMALY_CORE;
            spread = ANOMALY_SPREAD;
        } else if (observed == Map.CaseType.RestrictedArea) {
            core = RESTRICTED_CORE;
            spread = RESTRICTED_SPREAD;
        } else {
            // Cellule normale: très faible dépôt (optionnel)
            core = 0;
            spread = 0;
        }

        depositWithDiffusion(x, y, core, spread);
    }

    /* Evaporation globale */
    public void evaporate() {
        final double factor = (1.0 - RHO);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tau[x][y] *= factor;
                // sécurité: tau ne tombe pas sous un minimum
                if (tau[x][y] < 0.0001) tau[x][y] = 0.0001;
            }
        }
    }

    /**
     * Download only at base : copie de tau.
     * IMPORTANT : appeler uniquement à la base.
     */
    public double[][] copyTau() {
        double[][] copy = new double[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(tau[x], 0, copy[x], 0, height);
        }
        return copy;
    }

    public double averageTau() {
        double sum = 0.0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sum += tau[x][y];
            }
        }
        return sum / (width * height);
    }

    private void depositWithDiffusion(int x, int y, double core, double spread) {
        tau[x][y] += core;

        // 4-neighborhood diffusion
        if (inside(x - 1, y)) tau[x - 1][y] += spread;
        if (inside(x + 1, y)) tau[x + 1][y] += spread;
        if (inside(x, y - 1)) tau[x][y - 1] += spread;
        if (inside(x, y + 1)) tau[x][y + 1] += spread;
    }

    private boolean inside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
