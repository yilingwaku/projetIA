package app;

import controller.SimulationController;
import model.drone.Drone;
import model.shared.Position;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
//        SimulationController simulationController = new SimulationController();
//        simulationController.run(3,1000);
        SimulationController controller = new SimulationController();
        controller.runDronesDemo(10, 300); // 10 ticks, 300ms pause
    }
}

