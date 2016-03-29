package macbeth;

import engine.Core;
import engine.Signal;
import examples.Premade2D;
import graphics.Graphics2D;
import graphics.data.Shader;
import graphics.data.Sprite;
import static graphics.loading.SpriteContainer.loadSprite;
import java.util.function.Supplier;
import util.Color4;
import util.RegisteredEntity;
import util.Vec2;

public class Person extends RegisteredEntity {

    public Signal<Vec2> position;
    public Signal<Vec2> velocity;
    public Signal<Double> rotation;
    public Signal<Vec2> prevPos;
    public final Sprite sprite;
    public Shader shader;

    public Person(String sprite, Vec2 pos, double rot) {
        this.sprite = new Sprite(sprite);
        position = Premade2D.makePosition(this);
        velocity = Premade2D.makeVelocity(this);
        rotation = Premade2D.makeRotation(this);
        position.set(pos);
        rotation.set(rot);
    }

//    public Person(String sprite) {
//        this.sprite = new Sprite(sprite);
//        position = Premade2D.makePosition(this);
//        velocity = Premade2D.makeVelocity(this);
//        rotation = Premade2D.makeRotation(this);
//    }
    @Override
    public void createInner() {
        //Simple components
        prevPos = addChild(new Signal(position.get()));//.combine(Core.delay(0, position)));
        add(Core.updateLayer(10).forEach(dt -> prevPos.set(position.get())));

        //Sprite graphics
        onUpdate(dt -> sprite.imageIndex += dt * sprite.imageSpeed);
        add(Core.renderLayer(1).onEvent(() -> {
            if (shader != null) {
                Shader.pushShader(shader);
            }
            sprite.draw(position.get(), rotation.get());
            if (shader != null) {
                Shader.popShader();
            }
        }));
    }

    public Signal<Vec2> footL, footR;

    public void createFeet() {
        Signal<Vec2> delta = Core.update.map(dt -> position.get().subtract(prevPos.get()).multiply(.33 / dt));
        Supplier<Vec2> prefFootL = () -> position.get().add(delta.get()).add(new Vec2(0, 30).rotate(rotation.get()));
        Supplier<Vec2> prefFootR = () -> position.get().add(delta.get()).add(new Vec2(0, -30).rotate(rotation.get()));
        Signal<Boolean> isFootL = new Signal(true);//Core.interval(.35).reduce(true, b -> !b);
        isFootL.throttle(.35).onEvent(() -> isFootL.edit(b -> !b));
        footL = new Signal(position.get());
        footR = new Signal(position.get());
        onUpdate(dt -> {
            if (prefFootL.get().subtract(footL.get()).length() > 120) {
                isFootL.set(true);
            }
            if (prefFootR.get().subtract(footR.get()).length() > 120) {
                isFootL.set(false);
            }
            if (isFootL.get()) {
                footL.edit(v -> v.interpolate(prefFootL.get(), Math.pow(.001, dt)));
            } else {
                footR.edit(v -> v.interpolate(prefFootR.get(), Math.pow(.001, dt)));
            }
        });
        add(delta, isFootL);
        add(Core.renderLayer(.5).onEvent(() -> {
            if (shader != null) {
                Shader.pushShader(shader);
            }
            Graphics2D.drawSprite(loadSprite("foot"), footL.get(), new Vec2(1), rotation.get(), sprite.color);
            Graphics2D.drawSprite(loadSprite("foot"), footR.get(), new Vec2(1, -1), rotation.get(), sprite.color);
            Graphics2D.drawWideLine(footL.get(), position.get().add(new Vec2(0, 30).rotate(rotation.get())), new Color4(0, .25, 0, sprite.color.a), 10);
            Graphics2D.drawWideLine(footR.get(), position.get().add(new Vec2(0, -30).rotate(rotation.get())), new Color4(0, .25, 0, sprite.color.a), 10);
            if (shader != null) {
                Shader.popShader();
            }
        }));
    }
}
