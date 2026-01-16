package scenario;

import model.environment.Map;
import model.shared.Position;

public class ScenarioUI implements Scenario {

    @Override
    public String name() { return "S0_UI"; }

    @Override
    public void apply(Map map, Position base, int width, int height) {
        // nothing: no anomalies
    }
}
