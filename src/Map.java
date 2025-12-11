public class Map {
    public static final int EMPTY = 0;
    public static final int ANOMALY = 1;

    private final int width;
    private final int height;
    private final int[][] grid;


    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[width][height];
    }

    public void generateRandomAnomalies(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            grid[y][x] = ANOMALY;
        }
    }


    public void printMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                switch (grid[x][y]) {
                    case EMPTY:
                        System.out.print(". "); break;
                    case ANOMALY:
                        System.out.print("X "); break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }

}

