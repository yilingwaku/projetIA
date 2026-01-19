package test;

import model.drone.Drone;
import model.shared.Position;

import java.util.*;

public class Evaluator {

    private final int width;
    private final int height;

    // Statistiques globales
    private int totalCollisions = 0;
    private int totalAnomaliesDetected = 0;
    private int totalAnalysisTime = 0;

    // Couverture
    private double coverage = 0.0;
    private final boolean[][] visited;
    private int visitedCount = 0;  // <- NEW: compteur incrémental

    // Statistiques par drone
    private final java.util.Map<Integer, DroneStats> droneStats;

    public Evaluator(int width, int height) {
        this.width = width;
        this.height = height;
        this.droneStats = new HashMap<>();
        this.visited = new boolean[width][height];
    }

    public static class DroneStats {
        public int collisions = 0;
        public int anomaliesDetected = 0;
        public int analysisTime = 0;
    }

    public void update(model.environment.Map map, List<Drone> drones) {

        for (Drone d : drones) {
            DroneStats stats = droneStats.computeIfAbsent(d.getId(), k -> new DroneStats());

            // NOTE: ici tu comptes "ANALYZE" chaque step.
            // Cela mesure plutôt "temps passé en analyse" que "nombre d'anomalies uniques".
            if (d.getState() == Drone.DroneState.ANALYZE) {
                stats.anomaliesDetected++;
                stats.analysisTime++;
                totalAnomaliesDetected++;
                totalAnalysisTime++;
            }

            // Collisions (attention: double comptage possible, mais on ne change pas ici)
            for (Drone other : drones) {
                if (d.getId() != other.getId() && d.getPosition().equals(other.getPosition())) {
                    stats.collisions++;
                    totalCollisions++;
                }
            }

            // Couverture: incrémental
            Position pos = d.getPosition();
            int x = pos.getX();
            int y = pos.getY();

            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (!visited[x][y]) {
                    visited[x][y] = true;
                    visitedCount++; // <- NEW
                }
            }
        }

        coverage = 100.0 * visitedCount / (width * height);
    }

    // ===== Getters pour affichage temps réel =====
    public double getCoverage() {
        return coverage;
    }

    public int getVisitedCount() {
        return visitedCount;
    }

    public int getTotalCells() {
        return width * height;
    }

    // Affichage du rapport final
    public void printReport() {
        System.out.println("===== EVALUATION =====");
        System.out.printf(java.util.Locale.US, "Couverture : %.2f%% (%d/%d)\n",
                coverage, visitedCount, width * height);
        System.out.println("Collisions : " + totalCollisions);
        System.out.println("Anomalies détectées : " + totalAnomaliesDetected);
        System.out.println("Temps total en analyse : " + totalAnalysisTime + "s");

        System.out.println("\n--- Par drone ---");
        for (java.util.Map.Entry<Integer, DroneStats> entry : droneStats.entrySet()) {
            DroneStats s = entry.getValue();
            System.out.println("Drone " + entry.getKey() + " : collisions=" + s.collisions +
                    ", anomalies détectées=" + s.anomaliesDetected +
                    ", temps analyse=" + s.analysisTime + "s");
        }
    }
}
