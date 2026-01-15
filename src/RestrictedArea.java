public class RestrictedArea extends Anomaly{
    public RestrictedArea(int width, int height){
        super(width, height);
    }

    /**
     * RestrictedArea est une anomalie fixe, il n'y a donc pas d'évolution
     */
    public void step(){

    }

    /**
     * @return le type de l'anomalie, soit RestrictedArea
     */
    public Map.CaseType getType(){
        return Map.CaseType.RestrictedArea;
    }
}
