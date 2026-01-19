package scenario;

import model.environment.Map;
import model.shared.Position;

import java.util.Random;

public class ScenarioResultFix implements Scenario{
    @Override
    public String name() {
        return "S5_RESULTFIX";
    }

    @Override
    public void apply(Map map, Position base, int width, int height) {
        // Restricted
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
}
