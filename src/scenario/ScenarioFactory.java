package scenario;

public class ScenarioFactory {
    public static Scenario create(ScenarioId id) {
        return switch (id) {
            case S0_COUVERTURE -> new ScenarioCouverture();
            case S1_DETECTION -> new ScenarioDetection();
            case S2_COORDINATION -> new ScenarioCoordination();
            case S4_ANALYZE -> new ScenarioAnalyze();
            case S5_RESULTFIX -> new ScenarioResultFix();
        };
    }
}
