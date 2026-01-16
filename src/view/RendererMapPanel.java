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

    private int time;
    private Map map;
    private List<Drone> drones;
    private Position base;

    public RendererMapPanel(int mapWidth, int mapHeight, int cellSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.cellSize = cellSize;

        setPreferredSize(new Dimension(
                mapWidth * cellSize,
                mapHeight * cellSize
        ));
        setBackground(Color.WHITE);
    }

    public void updateState(int time,
                            Map map,
                            List<Drone> drones,
                            Position base) {
        this.time = time;
        this.map = map;
        this.drones = drones;
        this.base = base;
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
}
