package model.environment;

public class Pollution extends Anomaly {
    public Pollution(int width, int height) {
        super(width, height);
    }

    /**
     * Déplacement de l'anomalie:
     * Case touché:
     *  - si 2 ou 3 voisins --> rien ne se passe
     *  - sinon disparait
     * Case non touché :
     *  - si 3 voisins --> anomalie apparaît
     *  - sinon rien ne se passe
     */
    public void step(){
        boolean[][] next = new boolean[width][height];
        for(int x = 0;x<width;x++){
            for(int y = 0;y<height;y++){
                int neighbors = countNeighbors(x,y);
                if(grid[x][y]){
                    next[x][y] = neighbors == 2 || neighbors==3;
                }else{
                    next[x][y] = neighbors== 3;
                }
            }
        }
        grid = next;
    }

    /**
     * @return le type de l'anomalie, soit model.environment.Pollution
     */
    public Map.CaseType getType(){
        return Map.CaseType.Pollution;
    }
}
