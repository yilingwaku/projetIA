package controller;

import model.center.ControlCenter;
import model.drone.Drone;
import model.environment.Map;
import model.shared.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test console complet (sans UI).
 */
public class SimulationController {

    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;
    private static final int DRONE_COUNT = 7;

    private static final int STEPS = 800;   // 800 secondes simulées
    private static final int SLEEP_MS = 0;  // 0 => très rapide

    public void run() throws InterruptedException {

        Position base = new Position(WIDTH / 2, HEIGHT / 2);

        // Environnement réel (dynamique)
        Map map = new Map(WIDTH, HEIGHT);

        // Pour rendre le test visible, on active quelques anomalies au départ
        map.activeAnomaly(3, 3, Map.CaseType.Pollution);
        map.activeAnomaly(15, 10, Map.CaseType.Collapse);
        map.activeAnomaly(10, 16, Map.CaseType.RestrictedArea);

        // Centre (tau global)
        ControlCenter center = new ControlCenter(WIDTH, HEIGHT);

        // Drones
        List<Drone> drones = new ArrayList<>();
        Random masterRng = new Random(42);
        for (int i = 0; i < DRONE_COUNT; i++) {
            drones.add(new Drone(
                    i,
                    base,
                    base,
                    new Random(masterRng.nextLong()),
                    WIDTH,
                    HEIGHT
            ));
        }

        // Simulation
        for (int t = 0; t < STEPS; t++) {

            // 1) Le monde évolue (anomalies dynamiques)
            map.step();

            for (Drone d : drones) {

                // 2) Drone agit (utilise tauLocal)
                d.step(WIDTH, HEIGHT);

                // 3) Observation locale réelle (exploration de catastrophe)
                int x = d.getPosition().getX();
                int y = d.getPosition().getY();
                Map.CaseType observed = map.isSafe(x, y);

                // 4) Le drone réagit (analyse si anomalie)
                d.observe(observed);

                // 5) Upload anytime : le drone rapporte au centre
                center.reportCell(x, y, observed);

                // 6) Download only at base : sync tau uniquement à la base
                if (d.getPosition().equals(base)) {
                    d.syncTau(center.copyTau());
                    System.out.println("[BASE] t=" + t + " drone=" + d.getId()
                            + " sync tau | avgTau=" + String.format("%.2f", center.averageTau()));
                }
            }

            // 7) Évaporation
            center.evaporate();

            // 8) Logs simples
            if (t % 50 == 0) {
                System.out.println("t=" + t + " avgTau=" + String.format("%.2f", center.averageTau())
                        + " | d0=" + drones.get(0).getPosition() + " " + drones.get(0).getState());
            }

            if (SLEEP_MS > 0) Thread.sleep(SLEEP_MS);
        }

        System.out.println("Simulation terminée.");
    }
}
