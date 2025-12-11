import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class Terrain {
    private ArrayList<ArrayList<Integer>> terrain=new ArrayList<>();
    private final int width=10,height=10;

    public Terrain(){
        Random random = new Random();
        for (int x=0;x<width;x++){
            ArrayList<Integer> vx = new ArrayList<>();
            for (int y=0;y<height;y++){
                vx.add(0);
            }
            terrain.add(vx);
        }
        terrain.get(5).set(5,1);
    }
    public void print(){
        for (int x=0;x<width;x++){
            for (int y=0;y<height;y++){
                System.out.printf(terrain.get(x).get(y).toString()+"\t");
            }
            System.out.println();
        }

    }

}
