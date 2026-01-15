package model.center;

import model.drone.DroneReport;
import model.environment.Map;

/**
 * Centre de contrôle :
 * - reçoit les rapports des drones à tout moment (upload)
 * - maintient une "carte globale connue" (connaissance, pas vérité)
 * - fournit un snapshot uniquement à la base (download)
 */
public class ControlCenter {

    private final int width;
    private final int height;

    // Carte "connue" : null = inconnue
    private final Map.CaseType[][] known;

    public ControlCenter(int width, int height) {
        this.width = width;
        this.height = height;
        this.known = new Map.CaseType[width][height];
    }

    /**
     * Réception d'un rapport drone (upload anytime).
     * On met à jour la connaissance globale.
     */
    public void receiveReport(DroneReport report) {
        int x = report.position.getX();
        int y = report.position.getY();
        if (x >= 0 && x < width && y >= 0 && y < height) {
            known[x][y] = report.observed;
        }
    }

    /**
     * Fournit un snapshot (download) : à utiliser uniquement quand le drone est à la base.
     * On renvoie une copie pour éviter les modifications externes.
     */
    public GlobalInformation createSnapshot() {
        Map.CaseType[][] copy = new Map.CaseType[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(known[x], 0, copy[x], 0, height);
        }
        return new GlobalInformation(copy);
    }

    /**
     * Optionnel : accès direct à une case connue (utile pour affichage console).
     */
    public Map.CaseType getKnownAt(int x, int y) {
        return known[x][y];
    }
}
