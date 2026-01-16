package view;

import model.center.ControlCenter;
import model.drone.Drone;
import model.environment.Map;
import model.shared.Position;

import java.util.*;

/**
 * Renderer View
 */
public class Renderer {

    private final int width;
    private final int height;
    private final boolean clearScreen;

    public Renderer(int width, int height, boolean clearScreen) {
        this.width = width;
        this.height = height;
        this.clearScreen = clearScreen;
    }

    public void render(int t,
                       Map map,
                       List<Drone> drones,
                       Position base,
                       ControlCenter center,
                       Deque<String> eventLog,
                       int eventLogSize) {

        if (clearScreen) clearScreen();

        System.out.println("==== MAP t=" + t + " | avgTau=" + fmt(center.averageTau()) + " ====");
        System.out.println("Legend: B=Base  P/C/R=Anomalies  0..6=Drones  *=Collision  A=Analyze  p/c/r=Drone on anomaly  a=Analyze on anomaly");

        // Grid
        char[][] grid = new char[height][width];
        for (int y = 0; y < height; y++) Arrays.fill(grid[y], '.');

        // anomalies + base
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == base.getX() && y == base.getY()) {
                    grid[y][x] = 'B';
                    continue;
                }
                Map.CaseType ct = map.isSafe(x, y);
                switch (ct) {
                    case Pollution -> grid[y][x] = 'P';
                    case Collapse -> grid[y][x] = 'C';
                    case RestrictedArea -> grid[y][x] = 'R';
                    case BASE -> grid[y][x] = 'B';
                    default -> {}
                }
            }
        }

        // drones
        int[][] count = new int[height][width];
        Drone[][] single = new Drone[height][width];

        for (Drone d : drones) {
            int x = d.getPosition().getX();
            int y = d.getPosition().getY();
            if (!inside(x, y)) continue;

            count[y][x]++;
            if (count[y][x] == 1) single[y][x] = d;
            else single[y][x] = null; // collision
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (count[y][x] >= 2) {
                    grid[y][x] = '*';
                    continue;
                }

                if (count[y][x] == 1) {
                    Drone d = single[y][x];

                    Map.CaseType under = map.isSafe(x, y);
                    boolean onAnomaly = (under == Map.CaseType.Pollution
                            || under == Map.CaseType.Collapse
                            || under == Map.CaseType.RestrictedArea);

                    if (d.getState() == Drone.DroneState.ANALYZE) {
                        grid[y][x] = onAnomaly ? 'a' : 'A';
                    } else {
                        if (onAnomaly) {
                            grid[y][x] = switch (under) {
                                case Pollution -> 'p';
                                case Collapse -> 'c';
                                case RestrictedArea -> 'r';
                                default -> (char) ('0' + (d.getId() % 10));
                            };
                        } else {
                            grid[y][x] = (char) ('0' + (d.getId() % 10));
                        }
                    }
                }
            }
        }

        // Information des drones
        List<String> panel = buildStatusPanelLines(drones);

        int panelHeight = Math.max(height, panel.size());
        for (int y = 0; y < panelHeight; y++) {
            String mapLine = (y < height) ? new String(grid[y]) : " ".repeat(width);
            String panelLine = (y < panel.size()) ? panel.get(y) : "";
            System.out.println(mapLine + "   " + panelLine);
        }

        // event log
        System.out.println("\n---- EVENTS (last " + eventLogSize + ") ----");
        if (eventLog == null || eventLog.isEmpty()) {
            System.out.println("(no events yet)");
        } else {
            // afficher sans modifier la deque
            int start = Math.max(0, eventLog.size() - eventLogSize);
            int i = 0;
            for (String e : eventLog) {
                if (i++ >= start) System.out.println(e);
            }
        }
    }

    private List<String> buildStatusPanelLines(List<Drone> drones) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("---- DRONES ----");

        drones.stream()
                .sorted(Comparator.comparingInt(Drone::getId))
                .forEach(d -> {
                    String extra = "";
                    if (d.getState() == Drone.DroneState.ANALYZE) {
                        extra = " analyzeLeft=" + d.getAnalyzeRemainingSec() + "s";
                    } else if (d.getState() == Drone.DroneState.RECHARGING) {
                        extra = " rechargeLeft=" + d.getRechargeRemainingSec() + "s";
                    }

                    lines.add(String.format(
                            java.util.Locale.US,
                            "d%d pos=%s state=%s rem=%ds%s",
                            d.getId(),
                            d.getPosition(),
                            d.getState(),
                            d.getRemainingActiveSec(),
                            extra
                    ));
                });

        return lines;
    }

    private boolean inside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private static void clearScreen() {
        // ANSI clear screen + move cursor home
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
