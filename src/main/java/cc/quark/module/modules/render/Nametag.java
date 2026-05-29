package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

/**
 * Nametag - shows the client player's own nametag in third-person view.
 * Vanilla hides it by default. The {@code always} setting makes it visible
 * even in first-person.
 */
public class Nametag extends Module {

    private final BoolSetting always = register(new BoolSetting(
            "Always", "Show own nametag even in first-person view", false));

    public Nametag() {
        super("Nametag", "Shows your own nametag when in third-person (vanilla hides it)", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        // In first-person mode the camera is attached to the player; skip unless Always
        boolean thirdPerson = mc.options != null && mc.options.getPerspective().isFirstPerson();
        if (!always.isEnabled() && thirdPerson) return;

        String name = mc.player.getName().getString();

        // Project the point above our own head
        Vec3d headPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getHeight() + 0.3, mc.player.getZ());
        double[] screen = RenderUtil.project(headPos);
        if (screen == null) return;

        DrawContext ctx = event.getDrawContext();
        int sx = (int) screen[0];
        int sy = (int) screen[1];
        int textW = mc.textRenderer.getWidth(name);
        RenderUtil.drawCustomText(ctx, name, sx - textW / 2, sy, 0xFFFFFFFF);
    }
}
