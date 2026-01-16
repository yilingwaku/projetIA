package test;

import model.drone.Drone;
import model.environment.Map;  
import model.shared.Position;

import java.util.*; 

public class Evaluator {

    private final int width;
    private final int height;

    // Statistiques pour l'ensemble des drones
    private int totalCollisions = 0;
    private int totalAnomaliesDetected = 0;
    private int totalAnalysisTime = 0;
    private double coverage = 0.0;

    // Statistiques par drone
    private final java.util.Map<Integer, DroneStats> droneStats; // <--- explicitement java.util.Map

    // Pour la couverture (pourcentage de cases visitées)
    private final boolean[][] visited;

    public Evaluator(int width, int height) {
        this.width = width;
        this.height = height;
        this.droneStats = new HashMap<>();
        this.visited = new boolean[width][height];
    }

    // Pour l'évaluation des stats (collisions, anomalie détectées, temps d'analyse)
    public static class DroneStats {
        public int collisions = 0;
        public int anomaliesDetected = 0;
        public int analysisTime = 0;
    }

    public void update(model.environment.Map map, List<Drone> drones) {

        for (Drone d : drones) {
            DroneStats stats = droneStats.computeIfAbsent(d.getId(), k -> new DroneStats());

            if (d.getState() == Drone.DroneState.ANALYZE) {
                stats.anomaliesDetected++;
                stats.analysisTime++;
                totalAnomaliesDetected++;
                totalAnalysisTime++;
            }

            for (Drone other : drones) {
                if (d.getId() != other.getId() && d.getPosition().equals(other.getPosition())) {
                    stats.collisions++;
                    totalCollisions++;
                }
            }

            Position pos = d.getPosition();
            if (!visited[pos.getX()][pos.getY()]) {
                visited[pos.getX()][pos.getY()] = true;
            }
        }

        int visitedCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (visited[x][y]) visitedCount++;
            }
        }
        coverage = 100.0 * visitedCount / (width * height);
    }

    // Affichage des stats
    public void printReport() {
        System.out.println("===== EVALUATION =====");
        System.out.printf("Couverture : %.2f%%\n", coverage);
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