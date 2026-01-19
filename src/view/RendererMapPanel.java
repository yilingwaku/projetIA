package view;

import model.drone.Drone;
import model.environment.Map;
import model.shared.Position;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

public class RendererMapPanel extends JPanel {

    private final int mapWidth;
    private final int mapHeight;
    private final int cellSize;

    private double coveragePercent = 0.0;
    private int visitedCount = 0;
    private int totalCells = 0;
    private int nbAnomalies = -1;
    private int nbAnomaliesVisited = -1;


    private int time;
    private Map map;
    private List<Drone> drones;
    private Position base;

    public RendererMapPanel(int mapWidth, int mapHeight, int cellSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.cellSize = cellSize;

        int legendWidth = 150; // largeur réservée pour la légende
        setPreferredSize(new Dimension(
            mapWidth * cellSize + legendWidth, 
            mapHeight * cellSize));

        setBackground(Color.WHITE);
    }

    public void updateState(int time,
                            Map map,
                            List<Drone> drones,
                            Position base,
                            double coveragePercent,
                            int visitedCount,
                            int totalCells) {
        this.time = time;
        this.map = map;
        this.drones = drones;
        this.base = base;

        this.coveragePercent = coveragePercent;
        this.visitedCount = visitedCount;
        this.totalCells = totalCells;
    }

    public void updateState(int time,
                            Map map,
                            List<Drone> drones,
                            Position base,
                            double coveragePercent,
                            int visitedCount,
                            int totalCells,
                            int nbAnomalies,
                            int nbAnomaliesVisited) {
        this.time = time;
        this.map = map;
        this.drones = drones;
        this.base = base;

        this.coveragePercent = coveragePercent;
        this.visitedCount = visitedCount;
        this.totalCells = totalCells;
        this.nbAnomalies = nbAnomalies;
        this.nbAnomaliesVisited = nbAnomaliesVisited;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (map == null) return;

        Graphics2D g2 = (Graphics2D) g;

        drawGrid(g2);
        drawAnomalies(g2);
        drawBase(g2);
        drawDrones(g2);
        drawTime(g2);
        drawLegend(g2);
        drawStats(g2);

    }

    private void drawGrid(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x <= mapWidth; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, mapHeight * cellSize);
        }
        for (int y = 0; y <= mapHeight; y++) {
            g.drawLine(0, y * cellSize, mapWidth * cellSize, y * cellSize);
        }
    }

    private void drawAnomalies(Graphics2D g) {
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                switch (map.isSafe(x, y)) {
                    case Pollution -> g.setColor(new Color(120, 200, 120));
                    case Collapse -> g.setColor(new Color(200, 80, 80));
                    case RestrictedArea -> g.setColor(new Color(255, 165, 0));
                    default -> {
                        continue;
                    }
                }
                g.fillRect(
                        x * cellSize,
                        y * cellSize,
                        cellSize,
                        cellSize
                );
            }
        }
    }

    private void drawBase(Graphics2D g) {
        if (base == null) return;

        g.setColor(Color.BLUE);
        g.fillRect(
                base.getX() * cellSize,
                base.getY() * cellSize,
                cellSize,
                cellSize
        );
    }

    private void drawDrones(Graphics2D g) {
        if (drones == null) return;

        for (Drone d : drones) {
            int x = d.getPosition().getX();
            int y = d.getPosition().getY();

            Color color = switch (d.getState()) {
                case ANALYZE -> Color.ORANGE;
                case RECHARGING -> Color.GRAY;
                default -> Color.CYAN;
            };

            g.setColor(color);
            g.fillOval(
                    x * cellSize + 2,
                    y * cellSize + 2,
                    cellSize - 4,
                    cellSize - 4
            );

            g.setColor(Color.BLACK);
            g.drawString(
                    String.valueOf(d.getId()),
                    x * cellSize + cellSize / 3,
                    y * cellSize + cellSize / 2
            );
        }
    }

    private void drawTime(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawString("t = " + time, 10, 15);
    }

    private void drawLegend(Graphics2D g) {
    int startX = mapWidth * cellSize + 10; // à droite de la grille
    int startY = 20;
    int boxSize = 15;
    int lineHeight = 20;

    // Liste des légendes
    String[] labels = {
            "Base",
            "Pollution",
            "Collapse",
            "RestrictedArea",
            "Drone ACTIVE",
            "Drone ANALYZE",
            "Drone RECHARGING"
    };

    Color[] colors = {
            Color.BLUE,
            new Color(120, 200, 120),
            new Color(200, 80, 80),
            new Color(255, 165, 0),
            Color.CYAN,
            Color.ORANGE,
            Color.GRAY
    };

    g.setColor(Color.BLACK);
    g.drawString("LÉGENDE :", startX, startY - 5);

    for (int i = 0; i < labels.length; i++) {
        g.setColor(colors[i]);
        g.fillRect(startX, startY + i * lineHeight, boxSize, boxSize);

        g.setColor(Color.BLACK);
        g.drawRect(startX, startY + i * lineHeight, boxSize, boxSize);

        g.drawString(labels[i], startX + boxSize + 5, startY + i * lineHeight + 12);
        }

    }
    private void drawStats(Graphics2D g) {
        int startX = mapWidth * cellSize + 10;
        int startY = 20 + 8 * 20;

        g.setColor(Color.BLACK);
        g.drawString("STATS :", startX, startY);

        g.drawString(String.format(java.util.Locale.US,
                "Couverture : %.2f%%", coveragePercent), startX, startY + 20);

        g.drawString(String.format(java.util.Locale.US,
                "Visitées : %d / %d", visitedCount, totalCells), startX, startY + 40);

        if(nbAnomalies != -1)
            g.drawString(String.format(java.util.Locale.US,
                "Anomalies analysées : %d / %d", nbAnomalies, nbAnomaliesVisited), startX, startY + 60);
    }

}
