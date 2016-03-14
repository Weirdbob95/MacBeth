package macbeth;

import graphics.Graphics2D;
import graphics.data.Sprite;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import util.Color4;
import static util.Color4.BLACK;
import util.Log;
import util.Util;
import util.Vec2;

public class Tile {

    public static final int GRID_SIZE = 50;
    public static final double TILE_SIZE = 50;

    public static Tile[][] grid;

    static {
        grid = new Tile[GRID_SIZE][GRID_SIZE];
        Util.forRange(0, GRID_SIZE, 0, GRID_SIZE, (x, y) -> grid[x][y] = new Tile(x, y));
    }

    public static Stream<Tile> all() {
        return IntStream.range(0, GRID_SIZE).boxed().flatMap(x -> IntStream.range(0, GRID_SIZE).mapToObj(y -> grid[x][y]));
    }

    public static void load(String file) {
        try {
            Files.readAllLines(Paths.get(file)).forEach(s -> {
                int x = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                int y = Integer.parseInt(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                double height = Double.parseDouble(s.substring(0, s.indexOf(" ")));
                s = s.substring(s.indexOf(" ") + 1);
                Sprite sprite = s.isEmpty() ? null : new Sprite(s);

                Tile t = Tile.grid[x][y];
                t.sprite = sprite;
            });
        } catch (Exception ex) {
            Log.error(ex);
        }
    }

    public static Vec2 size() {
        return new Vec2(GRID_SIZE * TILE_SIZE);
    }

    public static Optional<Tile> tileAt(Vec2 pos) {
        if (pos.containedBy(new Vec2(0), size())) {
            return Optional.of(grid[(int) (pos.x / TILE_SIZE)][(int) (pos.y / TILE_SIZE)]);
        }
        return Optional.empty();
    }

    public static List<Tile> tilesNear(Vec2 pos, int amt) {
        List<Tile> r = new LinkedList();
        Util.forRange(0, amt, 0, amt, (x, y) -> {
            tileAt(pos.add(new Vec2(x, y).subtract(new Vec2(amt / 2. - .5)).multiply(TILE_SIZE))).ifPresent(r::add);
        });
        return r;
    }

    public int x, y;
    public Sprite sprite;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Color4 color() {
        return Color4.gray(height / 64 + .5);
    }

    public void draw2D() {
        if (sprite != null) {
            sprite.color = color();
            sprite.draw(new Vec2(x, y).multiply(TILE_SIZE), 0);
        } else {
            Graphics2D.fillRect(new Vec2(x, y).multiply(TILE_SIZE), new Vec2(TILE_SIZE), color());
        }
        Graphics2D.drawRect(new Vec2(x, y).multiply(TILE_SIZE), new Vec2(TILE_SIZE), BLACK);
    }
}
