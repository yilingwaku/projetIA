package model.center;

import model.environment.Map;


public class GlobalInformation {
    public final Map.CaseType[][] knownState;

    public GlobalInformation(Map.CaseType[][] knownState) {
        this.knownState = knownState;
    }
}
