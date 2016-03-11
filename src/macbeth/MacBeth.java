package macbeth;

import engine.Core;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Shader;
import graphics.loading.SpriteContainer;
import org.lwjgl.opengl.Display;
import static util.Color4.BLACK;
import static util.Color4.WHITE;
import static util.Color4.gray;
import util.Vec2;

public class MacBeth {

    public static void main(String[] args) {
        Core.init();
        Window2D.background = gray(.5);
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));

        Shader vision = new Shader("default.vert", "vision.frag");
        Core.time().forEach(t -> vision.setFloat("time", t));

        SpriteContainer.loadSprite("white_pixel").bind();

        Core.renderLayer(15).onEvent(() -> vision.with(() -> {
            Graphics2D.fillRect(new Vec2(-100), new Vec2(200), WHITE);
            Graphics2D.drawRect(new Vec2(-100), new Vec2(200), BLACK);
        }));

//        Core.renderLayer(10).onEvent(() -> Shader.pushShader(vision));
//        Core.renderLayer(20).onEvent(() -> Shader.popShader());
        Core.run();
    }
}
