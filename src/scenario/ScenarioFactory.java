package scenario;

public class ScenarioFactory {
    public static Scenario create(ScenarioId id) {
        return switch (id) {
            case S0_UI -> new ScenarioUI();
            case S1_ANALYZE -> new ScenarioAnalyze();
        };
    }
}
