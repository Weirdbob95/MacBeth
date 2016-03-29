package macbeth;

import engine.Core;
import engine.Signal;
import examples.Premade2D;
import graphics.Graphics2D;
import graphics.data.Shader;
import static graphics.loading.SpriteContainer.loadSprite;
import static macbeth.Game.vision;
import static util.Color4.WHITE;
import util.RegisteredEntity;
import util.Vec2;

public class Dagger extends RegisteredEntity {

    public Signal<Vec2> position;
    public Signal<Double> rotation;

    public Dagger() {
        position = Premade2D.makePosition(this);
        rotation = Premade2D.makeRotation(this);
    }

    @Override
    public void createInner() {
        onUpdate(dt -> rotation.set(position.get().subtract(RegisteredEntity.get(Macbeth.class).get().position.get()).direction()));

        Signal<Vec2> pastPos1 = Core.delay(.05, position);
        pastPos1.set(position.get());
        Signal<Vec2> pastPos2 = Core.delay(.1, position);
        pastPos2.set(position.get());
        Signal<Vec2> pastPos3 = Core.delay(.15, position);
        pastPos3.set(position.get());

        add(Core.render.onEvent(() -> {
            Shader.pushShader(vision);
            Graphics2D.drawSprite(loadSprite("dagger"), pastPos3.get(), new Vec2(1), rotation.get(), WHITE.withA(.05));
            Graphics2D.drawSprite(loadSprite("dagger"), pastPos2.get(), new Vec2(1), rotation.get(), WHITE.withA(.1));
            Graphics2D.drawSprite(loadSprite("dagger"), pastPos1.get(), new Vec2(1), rotation.get(), WHITE.withA(.2));
            Graphics2D.drawSprite(loadSprite("dagger"), position.get(), new Vec2(1), rotation.get(), WHITE);
            Shader.popShader();
        }));
    }
}
