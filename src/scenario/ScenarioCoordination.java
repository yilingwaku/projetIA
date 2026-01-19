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

        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                map.activeAnomaly(2 + dx, 2 + dy, Map.CaseType.Pollution);
            }
        }
    }
}
