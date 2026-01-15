package model.world;

import model.center.ControlCenter;
import model.center.GlobalInformation;
import model.drone.Drone;
import model.drone.DroneReport;
import model.environment.Map;
import model.shared.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * World = état global du système simulé.
 * Contient : environnement réel (Map), drones, centre de contrôle, temps.
 */
public class World {

    private final int width = 30;
    private final int height = 30;
    private final int droneCount = 7;

    private final Position basePosition;
    private final List<Drone> drones;

    // Ground truth (ami)
    private final Map environment;

    // Connaissance globale (centre)
    private final ControlCenter center;

    // Temps simulé (en secondes)
    private int timeSec;

    public World(long seed) {
        this.basePosition = new Position(width / 2, height / 2);

        // Environnement réel
        this.environment = new Map(width, height);

        // Exemple : activer quelques anomalies initiales (optionnel)
        // environment.activeAnomaly(...)

        // Centre
        this.center = new ControlCenter(width, height);

        // Drones
        Random master = new Random(seed);
        List<Drone> tmp = new ArrayList<>();
        for (int i = 1; i <= droneCount; i++) {
            Random rng = new Random(master.nextLong());
            tmp.add(new Drone(i, basePosition, basePosition, rng));
        }
        this.drones = tmp;

        this.timeSec = 0;
    }

    public void step() {
        // 1) évolution de l'environnement
        environment.step();

        // 2) chaque drone agit + envoie rapport
        for (Drone d : drones) {
            d.step(width, height);

            // Observation locale : uniquement la case courante
            int x = d.getPosition().getX();
            int y = d.getPosition().getY();
            Map.CaseType observed = environment.isSafe(x, y);

            // Upload anytime
            DroneReport report = new DroneReport(d.getId(), timeSec, d.getPosition(), observed);
            center.receiveReport(report);

            // Download only at base
            if (d.getPosition().equals(basePosition)) {
                GlobalInformation snap = center.createSnapshot();
                // Pour l'instant : on ne stocke pas encore dans le drone (pas de LocalMap),
                // on pourra faire d.sync(snap) à l'étape suivante.
                // Ici on peut juste laisser un hook.
            }
        }

        timeSec += 1;
    }

    public List<Drone> getDrones() {
        return Collections.unmodifiableList(drones);
    }

    public Position getBasePosition() {
        return basePosition;
    }

    public int getTimeSec() {
        return timeSec;
    }

    public Map getEnvironment() {
        return environment;
    }

    public ControlCenter getCenter() {
        return center;
    }
}
