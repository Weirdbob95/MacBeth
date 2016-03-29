package macbeth;

import engine.AbstractEntity;
import engine.Core;
import engine.EventStream;
import examples.Premade2D;
import graphics.data.Sprite;
import static macbeth.Killable.Bloodstain.splatter;
import macbeth.Macbeth.MacbethBullet;
import static macbeth.World.WORLD;
import util.Color4;
import util.RegisteredEntity;
import util.Util;
import util.Vec2;

public class Killable extends Person {

    public EventStream onDeath = new EventStream();
    private final Vec2 size;

    public Killable(String sprite, Vec2 pos, Vec2 size, double rot) {
        super(sprite, pos, rot);
        this.size = size;
    }

    @Override
    public void createInner() {
        super.createInner();
        createFeet();

        add(Core.update.filter(dt -> RegisteredEntity.getAll(MacbethBullet.class).stream()
                .anyMatch(mb -> mb.pos.get().containedBy(position.get().add(size), position.get().subtract(size)))).first(1).onEvent(() -> {
            RegisteredEntity.getAll(MacbethBullet.class).stream().filter(mb -> mb.pos.get().containedBy(position.get().add(size), position.get().subtract(size))).findFirst().get().destroy();
            Core.time().doForEach(t -> sprite.color = sprite.color.withA(.5 - t / 2));
            Core.timer(1, this::destroy);
            splatter(position.get(), 5);
            RegisteredEntity.get(Macbeth.class).get().blood.edit(i -> i + 1);
            onDeath.sendEvent();
        }));
        add(Core.update.filter(dt -> RegisteredEntity.getAll(Dagger.class).stream()
                .anyMatch(d -> d.position.get().containedBy(position.get().add(size), position.get().subtract(size)))).first(1).onEvent(() -> {
            Core.time().doForEach(t -> sprite.color = sprite.color.withA(.5 - t / 2));
            Core.timer(1, this::destroy);
            splatter(position.get(), 5);
            RegisteredEntity.get(Macbeth.class).get().blood.edit(i -> i + 1);
            onDeath.sendEvent();
        }));
    }

    public static class Bloodstain extends AbstractEntity {

        public Bloodstain() {
            Premade2D.makePosition(this);
            Premade2D.makeRotation(this);
            Premade2D.makeSpriteGraphics(this, "blood").get().color = new Color4(1, 1, 1, .5);
        }

        @Override
        public void create() {
        }

        public static void splatter(Vec2 pos, int num) {
            splatter(pos, num, WORLD);
        }

        public static void splatter(Vec2 pos, int num, World world) {
            Util.repeat(num, () -> {
                Bloodstain b = world.addObject(new Bloodstain());
                b.get("position", Vec2.class).set(pos.add(Vec2.randomCircle(10 * num)));
                b.get("rotation", Double.class).set(Math.random() * 2 * Math.PI);
                b.get("sprite", Sprite.class).get().scale = new Vec2(Math.sqrt(num) / 2);
            });
        }
    }
}
