package view;

import model.center.ControlCenter;
import model.drone.Drone;
import model.environment.Map;
import model.shared.Position;

import javax.swing.*;
import java.util.Deque;
import java.util.List;

// Fenêtre graphique pour la simulation 

public class RendererSimulationSwing extends JFrame {

    private final RendererMapPanel mapPanel;

    public RendererSimulationSwing(int width, int height, int cellSize) {
        super("Simulation de Drones");

        this.mapPanel = new RendererMapPanel(width, height, cellSize);

        // Configuration de la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width * cellSize + 200, height * cellSize + 50); // espace pour légende
        setLocationRelativeTo(null);

        // Ajouter directement le panneau de rendu
        setContentPane(mapPanel);

        setVisible(true);
    }

    public void render(int t,
                       Map map,
                       List<Drone> drones,
                       Position base,
                       ControlCenter center,
                       Deque<String> eventLog,
                       int eventLogSize) {
        // Mettre à jour l'état de la carte et des drones
        mapPanel.updateState(t, map, drones, base);

        // Redessiner
        mapPanel.repaint();
    }
}
