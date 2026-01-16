package view;

import model.center.ControlCenter;
import model.drone.Drone;
import model.environment.Map;
import model.shared.Position;

import javax.swing.JFrame;
import java.util.Deque;
import java.util.List;

public class RendererSimulationSwing extends JFrame {

    private final RendererMapPanel mapPanel;

    public RendererSimulationSwing(int width, int height, int cellSize) {
        this.mapPanel = new RendererMapPanel(width, height, cellSize);

        setTitle("Autonomous Drone Swarm Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(mapPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void render(int t,
                       Map map,
                       List<Drone> drones,
                       Position base,
                       ControlCenter center,
                       Deque<String> eventLog,
                       int eventLogSize) {

        mapPanel.updateState(t, map, drones, base);
        mapPanel.repaint();
    }
}
