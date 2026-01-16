package scenario;

import model.environment.Map;
import model.shared.Position;

public interface Scenario {
    String name();
    void apply(Map map, Position base, int width, int height);
}
