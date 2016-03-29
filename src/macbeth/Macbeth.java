package macbeth;

import engine.Core;
import engine.EventStream;
import engine.Input;
import engine.Signal;
import examples.Premade2D;
import graphics.Window2D;
import macbeth.Killable.Bloodstain;
import static macbeth.World.WORLD;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;
import static util.Vec2.ZERO;

public class Macbeth extends Person {

    public Signal<Integer> blood = new Signal(0);

    public Macbeth() {
        super("macbeth", ZERO, Math.PI * 1.5);
    }

    @Override
    public void createInner() {
        super.createInner();
        Premade2D.makeWASDMovement(this, 200);

        Input.whileMouseDown(2).forEach(dt -> position.set(Input.getMouse()));

        //Collisions
        onUpdate(dt -> {
            if (!WORLD.isOpen(position.get(), 40)) {
                Vec2 delta = position.get().subtract(prevPos.get()).divide(10);
                position.set(prevPos.get());
                for (int i = 0; i < 10; i++) {
                    position.edit(delta.withX(0)::add);
                    if (!WORLD.isOpen(position.get(), 40)) {
                        position.edit(delta.withX(0).reverse()::add);
                        velocity.edit(v -> v.withY(0));
                        break;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    position.edit(delta.withY(0)::add);
                    if (!WORLD.isOpen(position.get(), 40)) {
                        position.edit(delta.withY(0).reverse()::add);
                        velocity.edit(v -> v.withX(0));
                        break;
                    }
                }
            }
        });

        //Face mouse
        onUpdate(dt -> rotation.set(Input.getMouse().subtract(position.get()).direction()));

        //View follows
        onUpdate(dt -> Window2D.viewPos = Window2D.viewPos.interpolate(position.get().interpolate(Input.getMouse(), .55), Math.pow(.01, dt)));

        //Shooting
        EventStream shoot = addChild(Input.whenMouse(0, true));
        shoot.onEvent(() -> WORLD.addObject(new MacbethBullet()));
        shoot.onEvent(() -> sprite.setSprite("macbeth_shoot"));
        shoot.throttle(.1).onEvent(() -> sprite.setSprite("macbeth"));

        //Blood
        Signal<Double> bloodTime = Core.time();
        bloodTime.filter(t -> t > 0).filter(blood.map(b -> b > 0)).onEvent(() -> {
            bloodTime.set(-5 * Math.random() / blood.get());
            Bloodstain.splatter(position.get(), 1);
        });
//        Signal<Double> time = Core.update.reduce(0., (dt, t) -> t + dt * Math.random());
//        return time.filter(t -> t > interval).forEach(t -> time.set(t - interval));

        //Feet
        createFeet();
    }

    public void teleport(Vec2 pos) {
        Window2D.viewPos = Window2D.viewPos.add(pos.subtract(position.get()));
        position.set(pos);
        prevPos.set(pos);
        footL.set(pos);
        footR.set(pos);
    }

    public class MacbethBullet extends RegisteredEntity {

        public Signal<Vec2> pos;

        @Override
        protected void createInner() {
            pos = Premade2D.makePosition(this);
            pos.set(position.get().add(new Vec2(110, -25).rotate(rotation.get())));
            Signal<Vec2> vel = Premade2D.makeVelocity(this);
            vel.set(new Vec2(1500, 0).rotate(rotation.get()));
            Premade2D.makeCircleGraphics(this, 5, Color4.gray(.2));
            pos.filter(v -> !WORLD.isOpen(v)).onEvent(this::destroy);
            Window2D.viewPos = Window2D.viewPos.subtract(vel.get().multiply(.005));
        }
    }
}
