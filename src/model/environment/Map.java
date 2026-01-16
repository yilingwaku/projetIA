package model.environment;

public class Map {
    private final int width,height;
    // Contient les trois types d'anomalie (model.environment.Pollution, Décombres, Zone interdite)
    private Anomaly[] anomalies;

    public enum CaseType{
        EMPTY,
        Pollution,
        Collapse,
        RestrictedArea,
        BASE
    }



    public Map(int width, int height){
        this.width = width;
        this.height=height;

        // Instanciation des gestionnaires d'anomalies
        initializeAnomaly();
    }

    /**
     * @param x
     * @param y
     * @return le type de la case
     */
    public CaseType isSafe(int x,int y){
        // Par défaut la case est vide
        CaseType r = CaseType.EMPTY;

        // Placer la BASE au centre de la map
        if (x == width/2 && y ==height/2) {
            return CaseType.BASE;
        }

        // Vérifier la présence de chaque type d'anomalie
        int i = 0;
        while (r == CaseType.EMPTY && i<anomalies.length){
            if(anomalies[i].isActive(x,y)){
                r = anomalies[i].getType();
            }
            i++;
        }

        return r;
    }

    /**
     * Activation de l'anomalie de type de coordonnés en paramètres effectifs
     * @param x
     * @param y
     * @param type
     */
    public void activeAnomaly(int x,int y, CaseType type){
        switch (type){
            case Pollution -> anomalies[0].setActive(x, y);
            case Collapse -> anomalies[1].setActive(x, y);
            case RestrictedArea -> anomalies[2].setActive(x, y);
        }
    }

    /**
     * @return l'état de la map, sous la forme d'un tableau définissant le type de chaque case
     */
    public CaseType[][] getState(){
        CaseType[][] grid = new CaseType[width][height];

        for (int x=0;x<width;x++){
            for (int y=0;y<height;y++){
                switch (isSafe(x,y)){
                    case BASE -> grid[x][y] = CaseType.BASE;
                    case EMPTY -> grid[x][y] = CaseType.EMPTY;
                    case Pollution -> grid[x][y] = CaseType.Pollution;
                    case Collapse -> grid[x][y] = CaseType.Collapse;
                    case RestrictedArea -> grid[x][y] = CaseType.RestrictedArea;
                }
            }
        }

        return grid;
    }

    /**
     * Mise à jour de l'état des anomalies à chaque étape
     */
    public void step(){
        for(Anomaly a : anomalies)
            a.step();
    }

    /**
     * Instanciation des gestionnaires d'anomalies
     */
    private void initializeAnomaly(){
        anomalies = new Anomaly[3];

        Pollution pollution = new Pollution(width,height);
        Collapse collapse = new Collapse(width,height);
        RestrictedArea restrictedArea = new RestrictedArea(width,height);

        anomalies[0] = pollution;
        anomalies[1] = collapse;
        anomalies[2] = restrictedArea;
    }
}
