package macbeth;

import engine.AbstractEntity;
import engine.Core;
import engine.Signal;
import graphics.Graphics2D;
import graphics.loading.SpriteContainer;
import java.util.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import util.Color4;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import util.RegisteredEntity;
import util.Sounds;
import util.Vec2;

public class World extends AbstractEntity {

    public static World WORLD;

    private final List<Room> rooms = new LinkedList();
    public Room last = null;
    public String texture;
    private final TreeMap<Double, Runnable> events = new TreeMap(Comparator.reverseOrder());
    private final Set<Integer> locked = new HashSet();
    private final List<AbstractEntity> objects = new LinkedList();
    public final List<Block> blocks = new LinkedList();

    @Override
    public void create() {
        WORLD = this;
        add(Core.renderLayer(-1).onEvent(() -> {
            glLineWidth(16);
            rooms.forEach(Room::drawBorder);
            rooms.forEach(Room::draw);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }));
        onUpdate(dt -> {
            RegisteredEntity.get(Macbeth.class).ifPresent(m -> {
                if (!events.isEmpty() && m.position.get().y < events.firstKey()) {
                    events.pollFirstEntry().getValue().run();
                }
            });
        });
        objects.forEach(AbstractEntity::create);
    }

    public <E extends AbstractEntity> E addObject(E e) {
        if (WORLD == this) {
            e.create();
        }
        addChild(e);
        objects.add(e);
        return e;
    }

    public Block block(Vec2 pos, Vec2 buffer) {
        Block b = new Block(last.LL.add(pos), buffer);
        blocks.add(b);
        return b;
    }

    public Signal<Boolean> collide(Vec2 pos, double buffer) {
        return addChild(Core.update.filter(() -> WORLD == this).map(() -> RegisteredEntity.get(Macbeth.class).map(m -> m.position.get().containedBy(pos.subtract(new Vec2(buffer)), pos.add(new Vec2(buffer)))).orElse(false)));
    }

    public void event(double pos, Runnable run) {
        events.put(last.LL.y + pos + Math.random() / 100, run);
    }

    public boolean isOpen(Vec2 pos) {
        return rooms.stream().anyMatch(r -> !locked.contains(r.id) && pos.containedBy(r.LL, r.LL.add(r.size)));
    }

    public boolean isOpen(Vec2 pos, double buffer) {
        return isOpen(pos.add(new Vec2(buffer))) && isOpen(pos.add(new Vec2(buffer, -buffer)))
                && isOpen(pos.add(new Vec2(-buffer))) && isOpen(pos.add(new Vec2(-buffer, buffer)))
                && !blocks.stream().anyMatch(b -> b.collide(pos, new Vec2(buffer)));
    }

    public void sound(double pos, String name) {
        event(pos, () -> Sounds.playSound(name));
    }

    //Rooms
    public void lock(int room) {
        locked.add(room);
    }

    public Room room(double width, double height) {
        double top = last == null ? 300 : last.LL.y;
        last = new Room(new Vec2(-width / 2, top - height), new Vec2(width, height));
        rooms.add(last);
        return last;
    }

    public int roomID() {
        return rooms.size() - 1;
    }

    public void unlock(int room) {
        locked.remove(room);
    }

    public class Room {

        public Vec2 LL;
        public Vec2 size;
        public String tex = texture;
        public int id = rooms.size();

        public Room(Vec2 LL, Vec2 size) {
            this.LL = LL;
            this.size = size;
        }

        public void draw() {
            glEnable(GL_TEXTURE_2D);
            SpriteContainer.loadSprite(tex).bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            WHITE.glColor();

            glBegin(GL_QUADS);
            {
                glTexCoord2d(0, 0);
                LL.add(size.withX(0)).glVertex();
                glTexCoord2d(0, size.y / 100);
                LL.glVertex();
                glTexCoord2d(size.x / 100, size.y / 100);
                LL.add(size.withY(0)).glVertex();
                glTexCoord2d(size.x / 100, 0);
                LL.add(size).glVertex();
            }
            glEnd();

            if (locked.contains(id)) {
                double width = Math.min(size.x, rooms.get(id - 1).size.x);
                Graphics2D.drawWideLine(LL.add(size.multiply(new Vec2(.5, 1))).subtract(new Vec2(width / 2, 0)),
                        LL.add(size.multiply(new Vec2(.5, 1))).add(new Vec2(width / 2, 0)), new Color4(.4, .2, 0), 10);
            }
        }

        public void drawBorder() {
            Graphics2D.drawRect(LL, size, BLACK);
        }
    }
}
