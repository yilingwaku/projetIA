package scenario;

import model.environment.Map;
import model.shared.Position;

public class ScenarioCoordination implements Scenario {

    @Override
    public String name() {
        return "S2_COORDINATION";
    }

    @Override
    public void apply(Map map, Position base, int width, int height) {

        // 主灾害（左上，大）
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                map.activeAnomaly(2 + dx, 2 + dy, Map.CaseType.Pollution);
            }
        }
    }
}
