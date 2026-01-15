package app;

import controller.SimulationController;
import model.drone.Drone;
import model.shared.Position;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
//        SimulationController simulationController = new SimulationController();
//        simulationController.run(3,1000);
        // Dimensions de la grille (exemple simple)
        int width = 10;
        int height = 10;

        // Position initiale (centre de la grille)
        Position start = new Position(width / 2, height / 2);

        // Générateur aléatoire (seed fixe pour reproductibilité)
        Random rng = new Random(42);

        // Création d'un drone
        Drone drone = new Drone(1, start, rng);
        System.out.println("Position initiale : " + drone.getPosition());

        for (int step = 1; step <= 20; step++) {
            drone.step(width, height);
            System.out.println(
                    "Step " + step +
                            " | Position = " + drone.getPosition() +
                            " | Etat = " + drone.getState()
            );
        }

        System.out.println("=== Test Drone : fin ===");
    }
}

