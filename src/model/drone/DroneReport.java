package model.drone;

import model.shared.Position;
import model.environment.Map;

/**
 * Rapport envoyé par un drone au centre de contrôle.
 * Upload : possible à tout moment (depuis la position actuelle).
 */
public class DroneReport {
    public final int droneId;
    public final int timeSec;          // temps simulé (en secondes)
    public final Position position;    // position courante
    public final Map.CaseType observed; // observation locale (cellule courante)

    public DroneReport(int droneId, int timeSec, Position position, Map.CaseType observed) {
        this.droneId = droneId;
        this.timeSec = timeSec;
        this.position = position;
        this.observed = observed;
    }
}
