import java.util.ArrayList;
import java.util.Random;

public class Terrain {
    private final int width=30,height=30;
    private final int[][] terrain=new int[width][height];

    enum CaseType{
        EMPTY,
        ANOMALIE,
        BASE
    }

    public Terrain(){
        initialize();
    }

    private void initialize(){
        Random random = new Random();

        for (int x=0;x<width;x++){
            for (int y=0;y<height;y++){
                terrain[x][y] = CaseType.EMPTY.ordinal();
            }
        }
        terrain[width/2][height/2] = CaseType.BASE.ordinal();

        for (int nbA = 0;nbA<10;nbA++){
            terrain[random.nextInt(width)][random.nextInt(height)]=CaseType.ANOMALIE.ordinal();
        }
    }

    public void print(){
        for (int x=0;x<width;x++){
            for (int y=0;y<height;y++){
                System.out.printf(
                        terrain[x][y]==0?".":
                        terrain[x][y]==1?"#":"|");
                System.out.printf("\t");
            }
            System.out.println();
        }
    }

    public int getCase(int x,int y){
        return terrain[x][y];
    }

}
