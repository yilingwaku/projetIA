package model.shared;

import java.util.Objects;

/**
 * Représente une position (x, y) sur la grille.
 * Immutable : une fois créée, la position ne change pas.
 */
public final class Position {
    private final int x;
    private final int y;

    /**
     * @param x coordonnée horizontale
     * @param y coordonnée verticale
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** @return coordonnée x */
    public int getX() {
        return x;
    }

    /** @return coordonnée y */
    public int getY() {
        return y;
    }

    /**
     * Crée une nouvelle position après un déplacement.
     * @param dx déplacement en x
     * @param dy déplacement en y
     * @return nouvelle Position
     */
    public Position translate(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
