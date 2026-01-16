package controller;

import model.center.ControlCenter;
import model.drone.Drone;
import model.environment.Map;
import model.shared.Position;
import scenario.Scenario;
import scenario.ScenarioFactory;
import scenario.ScenarioId;
import view.Renderer;
import view.RendererSimulationSwing;


import java.util.*;

/**
 * SimulationController
 * Choisir scenario, creer models, execute la programmation
 * - Scenario pour initialization de MAP
 * - Renderer pour UI
 */
public class SimulationController {

    // GLOBAL CONFIG
    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;
    private static final int DRONE_COUNT = 7;

    // Pour simuler RETURNING + RECHARGING
    private static final int STEPS = 2600;

    private static final int SLEEP_MS = 80;          // 0
    private static final int RENDER_EVERY = 1;     // render chaque 1 seconds
    private static final boolean CLEAR_SCREEN = true;
    private static final int EVENT_LOG_SIZE = 14;

    // Choix de scenario
//    private static final ScenarioId SCENARIO_ID = ScenarioId.S0_UI;
    private static final ScenarioId SCENARIO_ID = ScenarioId.S1_ANALYZE;

    // =============================================

    private final Deque<String> eventLog = new ArrayDeque<>();


    // Vue graphique de la simulation (affichage en temps réel de la carte, des drones et des anomalies)
    private RendererSimulationSwing rendererSwing;

    public void run() throws InterruptedException {

        // Position de la base au milieu de MAP
        Position base = new Position(WIDTH / 2, HEIGHT / 2);

        // Environment + control center
        Map map = new Map(WIDTH, HEIGHT);
        ControlCenter center = new ControlCenter(WIDTH, HEIGHT);

        // Scenario
        Scenario scenario = ScenarioFactory.create(SCENARIO_ID);
        scenario.apply(map, base, WIDTH, HEIGHT);
        pushEvent("[SCENARIO] " + scenario.name());

        // Model: drones
        List<Drone> drones = new ArrayList<>();
        Random master = new Random(42);
        for (int i = 0; i < DRONE_COUNT; i++) {
            drones.add(new Drone(
                    i,
                    base,
                    base,
                    new Random(master.nextLong()),
                    WIDTH,
                    HEIGHT
            ));
        }

        // View console
        Renderer renderer = new Renderer(WIDTH, HEIGHT, CLEAR_SCREEN);

        //View graphique
        int cellSize = 20; 
        rendererSwing = new RendererSimulationSwing(WIDTH, HEIGHT, cellSize);

        // Boucle
        for (int t = 0; t < STEPS; t++) {

            // WORLD STEP
            map.step();

            // DRONES STEP
            for (Drone d : drones) {

                Drone.DroneState before = d.getState();

                // Deplacement de drone
                d.step(WIDTH, HEIGHT);

                int x = d.getPosition().getX();
                int y = d.getPosition().getY();

                // Observation
                Map.CaseType observed = map.isSafe(x, y);

                // Reaction: S'il y a un anomaly alors ACTIVE => ANALYZE
                d.observe(observed);

                // Renvoyer l'information au centre
                center.reportCell(x, y, observed);

                // Si drone retoure a la base , il met a jour la pheromone grobale
                if (d.getPosition().equals(base)) {
                    d.syncTau(center.copyTau());
                    pushEvent("[BASE SYNC] t=" + t + " d" + d.getId()
                            + " avgTau=" + fmt(center.averageTau()));
                }

                // Changer evenements
                Drone.DroneState after = d.getState();
                if (after != before) {
                    String msg = buildStateChangeMsg(t, d, before, after, observed);
                    if (!msg.isEmpty()) pushEvent(msg);
                }
            }

            // EVAPORATION GLOBALE
            center.evaporate();

            // RENDER ui
            if (t % RENDER_EVERY == 0) {
                renderer.render(t, map, drones, base, center, eventLog, EVENT_LOG_SIZE); // console
                rendererSwing.render(t, map, drones, base, center, eventLog, EVENT_LOG_SIZE);  // graphique
            }

            if (SLEEP_MS > 0) Thread.sleep(SLEEP_MS);
        }

        System.out.println("Simulation terminée.");
    }


    private void pushEvent(String s) {
        eventLog.addLast(s);
        while (eventLog.size() > EVENT_LOG_SIZE) eventLog.removeFirst();
    }

    private static String buildStateChangeMsg(int t, Drone d,
                                              Drone.DroneState before,
                                              Drone.DroneState after,
                                              Map.CaseType observed) {

        String pos = d.getPosition().toString();

        if (after == Drone.DroneState.ANALYZE) {
            return "[DETECT] t=" + t + " d" + d.getId()
                    + " détecte " + observed + " à " + pos + " -> ANALYZE (10s)";
        } else if (after == Drone.DroneState.RETURNING) {
            return "[RETURN] t=" + t + " d" + d.getId()
                    + " -> RETURNING | remaining=" + d.getRemainingActiveSec() + "s";
        } else if (after == Drone.DroneState.RECHARGING) {
            return "[RECHARGE] t=" + t + " d" + d.getId()
                    + " -> RECHARGING (600s)";
        } else if (after == Drone.DroneState.ACTIVE && before == Drone.DroneState.RECHARGING) {
            return "[READY] t=" + t + " d" + d.getId()
                    + " recharge terminée -> ACTIVE";
        } else if (after == Drone.DroneState.ACTIVE && before == Drone.DroneState.ANALYZE) {
            return "[ANALYZE END] t=" + t + " d" + d.getId()
                    + " analyse terminée -> ACTIVE";
        }

        return "";
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
