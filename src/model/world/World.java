package model.world;

import model.drone.Drone;
import model.shared.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Contient l'état global minimal : dimensions + liste de drones.
 * On construit d'abord une base : 7 drones qui bougent dans une grille.
 */
public class World {

    private final int width = 10;
    private final int height = 10;
    private final int droneCount = 7;
    private final List<Drone> drones;

    /**
     * @param seed graine pour reproductibilité
     */
    public World(long seed) {

        // Base au centre
        Position base = new Position(width / 2, height / 2);

        // Random global, puis un Random par drone (reproductible et stable)
        Random master = new Random(seed);

        List<Drone> tmp = new ArrayList<>();
        for (int i = 1; i <= droneCount; i++) {
            // Chaque drone a son propre RNG dérivé
            Random rng = new Random(master.nextLong());
            tmp.add(new Drone(i, base, rng));
        }
        this.drones = tmp;
    }

    /** Avance d'un tick (temps simulé) */
    public void step() {
        for (Drone d : drones) {
            d.step(width, height);
        }
    }

    /** @return liste non modifiable des drones */
    public List<Drone> getDrones() {
        return Collections.unmodifiableList(drones);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
