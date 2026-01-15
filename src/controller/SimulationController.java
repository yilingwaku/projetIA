package controller;

import model.environment.Map;
import model.world.World;
import model.drone.Drone;



public class SimulationController {
    private final Map map;
    private final int width = 30,height = 30;

    public SimulationController(){
        // Création de la map
        map = new Map(width,height);

        // Installation des anomalies
        // De type model.environment.Pollution
        map.activeAnomaly(5,5, Map.CaseType.Pollution);
        map.activeAnomaly(6,5, Map.CaseType.Pollution);
        map.activeAnomaly(5,4, Map.CaseType.Pollution);
        map.activeAnomaly(3,5, Map.CaseType.Pollution);

        // De type model.environment.RestrictedArea
        map.activeAnomaly(2,5, Map.CaseType.RestrictedArea);
        map.activeAnomaly(25,6, Map.CaseType.RestrictedArea);
        map.activeAnomaly(25,7, Map.CaseType.RestrictedArea);

        // De type model.environment.Collapse
        map.activeAnomaly(5,25, Map.CaseType.Collapse);
        map.activeAnomaly(6,25, Map.CaseType.Collapse);
        map.activeAnomaly(6,24, Map.CaseType.Collapse);
        map.activeAnomaly(4,24, Map.CaseType.Collapse);
        map.activeAnomaly(3,24, Map.CaseType.Collapse);
        map.activeAnomaly(5,23, Map.CaseType.Collapse);
    }

    /**
     * Lancement de l'application
     * Affichage de la grille
     * Puis, mise à jour de l'état de la map
     * @param nbStep nombre d'étapes à parcourir
     * @param timeBetweenStep délai entre chaque étape
     */
    public void run(int nbStep, int timeBetweenStep){
        for(int step = 0;step<nbStep;step++){
            Map.CaseType[][] grid = map.getState();
            print(grid);
            try {
                Thread.sleep(timeBetweenStep);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            map.step();
        }
    }

    public void runDronesDemo(int steps, long sleepMs) {
        World world = new World(12345L);

        System.out.println("=== Simulation Drones (demo) ===");
        System.out.println("Grille: " + world.getWidth() + "x" + world.getHeight()
                + " | Drones: " + world.getDrones().size());

        for (int t = 1; t <= steps; t++) {
            world.step();

            System.out.println("\nTick " + t);
            for (Drone d : world.getDrones()) {
                System.out.println(" - D" + d.getId()
                        + " | pos=" + d.getPosition()
                        + " | etat=" + d.getState());
            }

            // Petite pause (temps réel) uniquement pour lire la console
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("\n=== Fin Simulation Drones (demo) ===");
    }

    /**
     * Affichage de l'état courant
     * @param grid
     */
    public void print(Map.CaseType[][] grid){
        for (int x=0;x<width;x++){
            for (int y=0;y<height;y++){
                System.out.print(
                        grid[x][y]== Map.CaseType.EMPTY ?".":
                        grid[x][y]== Map.CaseType.BASE?"|":
                        grid[x][y]== Map.CaseType.Pollution?"P":
                        grid[x][y]== Map.CaseType.Collapse?"C":"R");
                System.out.print("\t");
            }
            System.out.println();
        }
        System.out.println("------------------------------------------------------------------------------");
    }
}
