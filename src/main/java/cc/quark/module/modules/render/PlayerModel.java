package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.joml.Quaternionf;

public class PlayerModel extends Module {

    private final IntSetting x = register(new IntSetting(
            "X", "HUD X position", 50, 0, 3000));

    private final IntSetting y = register(new IntSetting(
            "Y", "HUD Y position", 50, 0, 3000));

    private final IntSetting scale = register(new IntSetting(
            "Scale", "3D model render scale", 40, 10, 120));

    public PlayerModel() {
        super("PlayerModel", "Shows 3D player model preview in corner", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int px = x.get();
        int py = y.get();
        int s = scale.get();

        // Background box
        ctx.fill(px - s / 2 - 4, py - s - 4, px + s / 2 + 4, py + 4, 0x88000000);

        drawPlayerModel(ctx, px, py, s);
    }

    private void drawPlayerModel(DrawContext ctx, int x, int y, int scale) {
        // Draw the player entity model using DrawContext.drawEntity
        float mouseX = x;
        float mouseY = y - scale;

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf bodyRot = new Quaternionf().rotateY((float) Math.toRadians(mc.player.getYaw() % 360));

        ctx.drawEntity(x, y, scale, -mouseX, -mouseY, mc.player);
    }
}
