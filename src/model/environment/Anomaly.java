package model.environment;

public abstract class Anomaly {
    // Présence de l'anomalie
    protected boolean[][] grid;
    protected final int width, height;

    public Anomaly(int width,int height){
        this.width = width;
        this.height = height;
        grid = new boolean[width][height];
    }

    /**
     * Activation de l'anomalie en fonction des paramètres effectifs
     * @param x
     * @param y
     */
    public void setActive(int x, int y){
        grid[x][y] = true;
    }

    /**
     * @param x
     * @param y
     * @return true si l'anomalie est active, false sinon
     */
    public boolean isActive(int x, int y){
        return grid[x][y];
    }

    public abstract void step();

    /**
     * @param x
     * @param y
     * @return le nombre des voisins à une distance de 1 par rapport à l'anomalie
     */
    protected int countNeighbors(int x, int y){
        int count = 0;
        for (int dx = -1;dx<=1;dx++)
            for (int dy = -1;dy<=1;dy++)
                if((dx!=0 || dy !=0) && (x+dx>=0 && y+dy>=0 && x+dx<width && y+dy<height && grid[dx+x][dy+y]))
                    count++;
        return count;
    }

    public abstract Map.CaseType getType();
}
