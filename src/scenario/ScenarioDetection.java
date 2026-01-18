package scenario;

import model.environment.Map;
import model.shared.Position;

public class ScenarioDetection implements Scenario {

    @Override
    public String name() {
        return "S1_DETECTION";
    }

    @Override
    public void apply(Map map, Position base, int width, int height) {

        int x = width - 6;
        int y = 2;

        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                map.activeAnomaly(x + dx, y + dy, Map.CaseType.Pollution);
            }
        }
    }
}
