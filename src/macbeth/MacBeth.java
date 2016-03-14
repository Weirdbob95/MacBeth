package macbeth;

import engine.AbstractEntity;
import engine.AbstractEntity.LAE;
import engine.Core;
import engine.Input;
import engine.Signal;
import examples.Premade2D;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Shader;
import graphics.loading.SpriteContainer;
import org.lwjgl.opengl.Display;
import static util.Color4.BLACK;
import static util.Color4.gray;
import util.Vec2;

public class MacBeth {

    public static void main(String[] args) {
        Core.init();
        Window2D.background = gray(.5);
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));

        Shader vision = new Shader("default.vert", "vision.frag");
        Core.time().forEach(t -> vision.setFloat("time", t));
        Core.render.onEvent(() -> vision.setVec2("mouse", Input.getMouseScreen().toFloatBuffer()));

        SpriteContainer.loadSprite("white_pixel").bind();

//        List<Vec2> l = new LinkedList();
//        Input.whenMouse(1, true).onEvent(() -> l.add(Input.getMouse()));
//
//        Core.renderLayer(15).onEvent(() -> vision.with(() -> {
//            Graphics2D.fillRect(new Vec2(-100), new Vec2(200), WHITE);
//            Graphics2D.drawRect(new Vec2(-100), new Vec2(200), BLACK);
//            l.forEach(v -> Graphics2D.fillEllipse(v, new Vec2(20), WHITE, 20));
//        }));
        sceneDagger();
        macbeth().create();

        Core.run();
    }

    public static AbstractEntity macbeth() {
        return new LAE(macbeth -> {
            Signal<Vec2> position = Premade2D.makePosition(macbeth);
            Premade2D.makeVelocity(macbeth);
            Premade2D.makeRotation(macbeth);
            Premade2D.makeSpriteGraphics(macbeth, "box");
            Premade2D.makeWASDMovement(macbeth, 200);
            macbeth.onUpdate(dt -> Window2D.viewPos = Window2D.viewPos.interpolate(position.get(), Math.pow(.1, dt)));
        });
    }

    public static void sceneDagger() {
        Vec2 LL = new Vec2(-400, -300);
        Vec2 UR = new Vec2(400, 300);
        Core.render.onEvent(() -> {
            Graphics2D.drawLine(LL, UR.withX(LL.x), BLACK, 8);
            Graphics2D.drawLine(UR, UR.withX(LL.x), BLACK, 8);
            Graphics2D.drawLine(UR, LL.withX(UR.x), BLACK, 8);
            Graphics2D.drawLine(LL, new Vec2(-100, -300), BLACK, 8);
            Graphics2D.drawLine(new Vec2(400, -300), new Vec2(100, -300), BLACK, 8);
            Graphics2D.fillRect(LL, UR.subtract(LL), gray(.9));
        });
    }
}
