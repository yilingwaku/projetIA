package app;

import controller.SimulationController;

/**
 * Point d'entrée principal du projet.
 */
public class Main {

    public static void main(String[] args) {
        try {
            SimulationController simulation = new SimulationController();
            simulation.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
