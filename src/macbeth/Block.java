package macbeth;

import graphics.Graphics2D;
import static util.Color4.RED;
import util.Vec2;
import static util.Vec2.ZERO;

public class Block {

    public Vec2 pos;
    public Vec2 buffer;

    public Block(Vec2 pos, Vec2 buffer) {
        this.pos = pos;
        this.buffer = buffer;
    }

    public boolean collide(Vec2 p, Vec2 b) {
        return p.subtract(b).quadrant(pos.add(buffer)) == 1 && pos.subtract(buffer).quadrant(p.add(b)) == 1;
    }

    public boolean contains(Vec2 p) {
        return collide(p, ZERO);
    }

    public void draw() {
        Graphics2D.fillRect(pos.subtract(buffer), buffer.multiply(2), RED.withA(.5));
    }

    @Override
    public String toString() {
        return "Block{" + "pos=" + pos + ", buffer=" + buffer + '}';
    }
}
