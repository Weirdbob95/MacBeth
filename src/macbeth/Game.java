package macbeth;

import engine.AbstractEntity.LAE;
import engine.*;
import examples.Premade2D;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.TextureAttachment;
import graphics.data.PostProcessEffect;
import graphics.data.Shader;
import java.util.Arrays;
import java.util.List;
import macbeth.Killable.Bloodstain;
import macbeth.World.Room;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import static util.Color4.BLACK;
import static util.Color4.gray;
import util.*;

public class Game {

    public static Shader vision;
    public static double dreamValue;

    public static void main(String[] args) {
        Core.init();
        Window2D.background = new Color4(0, .2, .05);
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));

        vision = new Shader("default.vert", "vision.frag");
        vision.setFloat("value", .2);
        vision.setFloat("mouseMult", 1);
        vision.setFloat("size", 40);
        Core.time().forEach(t -> vision.setFloat("time", t));
        Core.render.onEvent(() -> vision.setVec2("mouse", Input.getMouseScreen().toFloatBuffer()));
        Core.render.onEvent(() -> vision.setVec2("offset", Window2D.viewPos.toFloatBuffer()));

        Shader dream = new Shader("default.vert", "vision.frag");
        dream.setFloat("mouseMult", 0);
        dream.setFloat("size", 200);
        Core.time().forEach(t -> dream.setFloat("time", -t));
        Core.render.onEvent(() -> dream.setVec2("offset", Window2D.viewPos.toFloatBuffer()));
        Core.renderLayer(-10).onEvent(() -> dream.setFloat("value", dreamValue));
//        Core.renderLayer(-10).onEvent(() -> Shader.pushShader(dream));
//        Core.renderLayer(10).onEvent(() -> Shader.popShader());
        new PostProcessEffect(1000, new Framebuffer(new TextureAttachment()), dream).create();

        world1();
        world2();

        world1.create();
        new Macbeth().create();

        Core.run();
    }

    public static World world1 = new World();
    public static World world2 = new World();

    public static void world1() {
        world1.texture = "grass_floor";

        Sounds.playSound("music1.mp3", true, .5);

        Input.whenKey(Keyboard.KEY_P, true).onEvent(() -> System.out.println(RegisteredEntity.getAll(Killable.class)));

        //The battle
        world1.room(1200, 1200);
        world1.addObject(new Killable("ally_pistol", world1.last.LL.add(new Vec2(1000, 1000)), new Vec2(50), 2.9 * Math.PI / 2));
        world1.addObject(new Killable("ally_pistol", world1.last.LL.add(new Vec2(300, 1000)), new Vec2(50), 3.1 * Math.PI / 2));
        world1.addObject(new Killable("ally_pistol", world1.last.LL.add(new Vec2(800, 800)), new Vec2(50), 2.9 * Math.PI / 2));
        world1.addObject(new Killable("ally_pistol", world1.last.LL.add(new Vec2(200, 700)), new Vec2(50), 3.2 * Math.PI / 2));
        world1.addObject(new Killable("ally_pistol", world1.last.LL.add(new Vec2(1000, 600)), new Vec2(50), 2.8 * Math.PI / 2));
        Killable macdonwald = world1.addObject(new Killable("enemy_pistol", world1.last.LL.add(new Vec2(600, 100)), new Vec2(50), Math.PI / 2));
        Block MDblock = world1.block(new Vec2(600, 100), new Vec2(100));
        macdonwald.onDeath.onEvent(() -> {
            world1.blocks.remove(MDblock);
            Sounds.stopSound("music1.mp3");
            Sounds.playSound("music1.mp3", true, .2);
            Sounds.playSound("worthy_gentleman.mp3");
        });
        world1.room(200, 400);

        //The three witches
        world1.room(800, 1600);
        Person witch1 = new Witch(world1.last.LL.add(new Vec2(100, 900)), 0);
        Person witch2 = new Witch(world1.last.LL.add(new Vec2(100, 600)), 0);
        Person witch3 = new Witch(world1.last.LL.add(new Vec2(100, 300)), 0);
        world1.sound(1600, "witch0.mp3");
        world1.event(1200, () -> {
            Arrays.asList(witch1, witch2, witch3).forEach(w -> w.create());
            dreamValue = .15;
        });
        world1.sound(1200, "witch4.mp3");
        world1.sound(900, "witch1.mp3");
        world1.sound(600, "witch2.mp3");
        world1.sound(300, "witch3.mp3");
        world1.sound(0, "witch4.mp3");
        world1.event(0, () -> {
            Arrays.asList(witch1, witch2, witch3).forEach(w -> w.destroy());
            dreamValue = 0;
        });
        world1.room(600, 400);

        world1.room(600, 600); //Ross
        Killable ross = world1.addObject(new Killable("ally", world1.last.LL.add(new Vec2(500, 300)), new Vec2(50), 0));
        ross.rotation.set(Math.PI);
        world1.sound(300, "ross1.mp3");
        world1.event(300, () -> Core.timer(2.5, () -> Sounds.playSound("ross2.mp3")));
        world1.room(400, 2000);
        world1.sound(1200, "ross3.mp3");
        Core.render.onEvent(() -> Shader.pushShader(vision));
        Bloodstain.splatter(world1.last.LL.add(new Vec2(200, 800)), 10, world1);
        Core.render.onEvent(() -> Shader.popShader());
        world1.event(800, () -> {
            Sounds.playSound("no_light.mp3");
            Mutable<Signal<Color4>> col = new Mutable(Core.time().map(t -> BLACK.withA(t / 3)));
            EventStream fade = Core.render.onEvent(() -> Graphics2D.fillRect(Window2D.LL(), Window2D.viewSize, col.o.get()));
            Core.timer(3, () -> {
                col.o.destroy();
                col.o = Core.time().map(t -> BLACK.withA(1 - t / 3));
                Core.timer(3, () -> {
                    col.o.destroy();
                    fade.destroy();
                });
                Window2D.background = gray(.3);
                world1.destroy();
                world2.create();
                RegisteredEntity.get(Macbeth.class).get().teleport(new Vec2(0));
            });
        });
    }

    public static void world2() {
        world2.texture = "stone_floor";
        world2.room(200, 1000);

        Room room_dagger = world2.room(1000, 800); //Dagger room
        world2.lock(room_dagger.id + 1);
        Dagger dagger = new Dagger();
        dagger.position.set(room_dagger.LL.add(new Vec2(500, 400)));
        world2.event(800, () -> {
            Sounds.playSound("dagger1.mp3");
            dreamValue = .15;
            dagger.create();
        });
        world2.collide(room_dagger.LL.add(new Vec2(500, 400)), 50).filter(b -> b).first(1).onEvent(() -> {
            Sounds.playSound("dagger3.mp3");
            dagger.onUpdate(dt -> dagger.position.edit(v -> v.interpolate(Input.getMouse(), Math.pow(.01, dt))));
            world2.unlock(room_dagger.id + 1);
        });
        world2.sound(0, "dagger2.mp3");
        world2.room(200, 1000);

        //Guards room
        Room room_guards = world2.room(400, 400);
        world2.lock(room_guards.id + 1);
        Killable guard1 = world2.addObject(new Killable("ally", world2.last.LL.add(new Vec2(300, 300)), new Vec2(50), Math.PI));
        Killable guard2 = world2.addObject(new Killable("ally", world2.last.LL.add(new Vec2(100, 100)), new Vec2(50), 0));
        guard1.onDeath.onEvent(() -> Sounds.playSound("guard1.mp3"));
        guard2.onDeath.onEvent(() -> Sounds.playSound("guard2.mp3"));
        guard1.onDeath.combineEventStreams(guard2.onDeath).count().filter(i -> i >= 2).first(1).onEvent(() -> world2.unlock(room_guards.id + 1));
        world2.room(200, 200);

        //Duncan's room
        Room room_duncan = world2.room(600, 400);
        world2.lock(room_duncan.id + 1);
        Killable duncan = world2.addObject(new Killable("ally", world2.last.LL.add(new Vec2(500, 200)), new Vec2(50), Math.PI));
        duncan.onDeath.first(1).onEvent(() -> {
            world2.unlock(room_duncan.id + 1);
            RegisteredEntity.get(Macbeth.class).get().blood.edit(i -> i + 10);
            Sounds.playSound("sleep_no_more.mp3");
        });
        world2.room(200, 500);
        world2.event(400, () -> {
            dreamValue = 0;
            dagger.destroy();
        });

        world2.room(600, 400); //Meet Lady Macbeth
        world2.addObject(new Person("lady_macbeth", world2.last.LL.add(new Vec2(500, 200)), Math.PI));
        world2.sound(200, "lady_macbeth.mp3");
        world2.room(200, 1500);
        world2.sound(1000, "knock.mp3");
        world2.event(1000, () -> Core.timer(2, () -> Sounds.playSound("wake_duncan.mp3")));

        //Feast
        Room feast = world2.room(1000, 1000);
        world2.addObject(new LAE(table -> {
            Premade2D.makePosition(table);
            table.get("position", Vec2.class).set(feast.LL.add(new Vec2(500, 490)));
            Premade2D.makeSpriteGraphics(table, "table");
        }));
        world2.block(new Vec2(500, 435), new Vec2(260, 200));

        world2.addObject(new Person("ally", feast.LL.add(new Vec2(305, 300)), 0));
        world2.addObject(new Person("ally", feast.LL.add(new Vec2(305, 435)), 0));
        world2.addObject(new Person("ally", feast.LL.add(new Vec2(305, 565)), 0));
        world2.addObject(new Person("ally", feast.LL.add(new Vec2(695, 300)), Math.PI));
        world2.addObject(new Person("ally", feast.LL.add(new Vec2(695, 435)), Math.PI));
        world2.addObject(new Person("ally", feast.LL.add(new Vec2(695, 565)), Math.PI));
        List<AbstractEntity> food = Arrays.asList(
                world2.addObject(new LAE(pizza -> {
                    Premade2D.makePosition(pizza);
                    Premade2D.makeVelocity(pizza);
                    pizza.get("position", Vec2.class).set(feast.LL.add(new Vec2(500, 500)));
                    Premade2D.makeSpriteGraphics(pizza, "pizza");
                })),
                world2.addObject(new LAE(fruit1 -> {
                    Premade2D.makePosition(fruit1);
                    Premade2D.makeVelocity(fruit1);
                    fruit1.get("position", Vec2.class).set(feast.LL.add(new Vec2(500, 390)));
                    Premade2D.makeSpriteGraphics(fruit1, "fruit_bowl");
                })),
                world2.addObject(new LAE(fruit2 -> {
                    Premade2D.makePosition(fruit2);
                    Premade2D.makeVelocity(fruit2);
                    fruit2.get("position", Vec2.class).set(feast.LL.add(new Vec2(500, 310)));
                    Premade2D.makeSpriteGraphics(fruit2, "fruit_bowl");
                })));

        world2.event(900, () -> {
            Sounds.playSound("banquo0.mp3");
            Core.timer(2, () -> {
                dreamValue = .2;
                Sounds.playSound("banquo1.mp3");
                Person banquo = world2.addObject(new Person("banquo", feast.LL.add(new Vec2(500, 700)), Math.PI / 2));
                banquo.shader = vision;
                banquo.onUpdate(dt -> banquo.rotation.set(RegisteredEntity.get(Macbeth.class).get().position.get().subtract(banquo.position.get()).direction()));
                food.forEach(e -> e.get("velocity", Vec2.class).set(Vec2.randomShell(500)));
                Core.timer(6, () -> Sounds.playSound("banquo2.mp3"));
                Core.timer(10, () -> {
                    Sounds.playSound("banquo3.mp3");
                    banquo.destroy();
                    dreamValue = 0;
                });
            });
        });
        world2.room(200, 1000);
    }
}
