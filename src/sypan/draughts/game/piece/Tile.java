package sypan.draughts.game.piece;

import java.util.regex.PatternSyntaxException;

import sypan.utility.Logger;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 * @author Carl Linley
 **/
public class Tile {

    private int x, y;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Tile() {
    }

    public Tile add(Tile add) {
        return new Tile(x + add.getX(), y + add.getY());
    }

    public Tile add(int x, int y) {
        return new Tile(this.x + x, this.y + y);
    }

    @Override
    public Tile clone() {
        return new Tile(x, y);
    }

    public Tile set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Tile subtract(int i, int j) {
        return new Tile(x - i, y - j);
    }

    public Tile subtract(Tile subtractBy) {
        return new Tile(x - subtractBy.getX(), y - subtractBy.getY());
    }

    public Tile normalise() {
        return new Tile(x >= 1 ? 1 : -1, y >= 1 ? 1 : -1);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isBlack() {
        return (x % 2 == 1 && y % 2 == 0) || ((x % 2 == 0 && y % 2 == 1));
    }

    public boolean equals(Tile t) {
        if (t == null) {
            return false;
        }
        return t.getX() == x && t.getY() == y;
    }

    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    public boolean outOfBounds() {
        return x < 0 || y < 0 || x > 7 || y > 7;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }

    public static Tile parseTile(String inputString) {
        try {
            String[] coordinates = inputString.split(",");
            return new Tile(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
        }
        catch (PatternSyntaxException | NumberFormatException e) {
            Logger.logWarning("Error parsing tile: '" + inputString + "': " + e);
            return null;
        }
    }

    public Vector3f getWorldLocation() {
        return new Vector3f(x * 2, 0, y * 2);
    }

    /**
     * Returns the distance to the specified tile. As {@code Tile} is in essence
     * a vector, calculating the distance is easy. There is, however, an offset
     * on the result - a simple check and subtraction solves this issue.
     *
     * @param destinationTile - the tile to check the distance to.
     * @return the distance to the specified {@code Tile} as an integer.
     **/
    public int distance(Tile destinationTile) {
        int differenceX = x - destinationTile.x,
            differenceY = y - destinationTile.y,
            distanceOffset = Math.round(FastMath.sqrt(FastMath.sqr(differenceX) + FastMath.sqr(differenceY)));

        if (distanceOffset == 1) {
            return 1;
        }
        else if (distanceOffset <= 5) {
            return distanceOffset - 1;
        }
        else if (distanceOffset < 10) {
            return distanceOffset - 2;
        }
        else {
            return distanceOffset - 3;
        }
    }
}