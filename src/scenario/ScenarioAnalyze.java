package scenario;

import model.environment.Map;
import model.shared.Position;

import java.util.Random;

public class ScenarioAnalyze implements Scenario {

    @Override
    public String name() { return "S1_ANALYZE"; }

    @Override
    public void apply(Map map, Position base, int width, int height) {

        // Pollution
        addAnomaly(map, 5, 5, Map.CaseType.Pollution, width, height);
        addAnomaly(map, 6, 5, Map.CaseType.Pollution, width, height);
        addAnomaly(map, 5, 4, Map.CaseType.Pollution, width, height);
        addAnomaly(map, 3, 5, Map.CaseType.Pollution, width, height);



        // Collapse
        addAnomaly(map, 25, 5, Map.CaseType.Collapse, width, height);
        addAnomaly(map, 25, 6, Map.CaseType.Collapse, width, height);
        addAnomaly(map, 25, 7, Map.CaseType.Collapse, width, height);


        // Restricted: many static points
        Random r = new Random(123);
        int target = 30;
        int placed = 0;
        while (placed < target) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            if (x == base.getX() && y == base.getY()) continue;
            map.activeAnomaly(x, y, Map.CaseType.RestrictedArea);
            placed++;
        }
    }


    private static void addAnomaly(Map map, int x, int y, Map.CaseType type, int w, int h) {
        map.activeAnomaly(x, y, type);
    }

}
