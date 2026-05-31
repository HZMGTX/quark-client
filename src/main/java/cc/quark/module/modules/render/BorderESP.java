package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.world.border.WorldBorder;

public class BorderESP extends Module {

    private final ColorSetting color        = register(new ColorSetting("Color", "World border visualization color", 0xFF4488FF));
    private final BoolSetting showDistance  = register(new BoolSetting("ShowDistance", "Show distance to world border", true));

    public BorderESP() {
        super("BorderESP", "Renders the world border as a visible colored wall", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        WorldBorder border = mc.world.getWorldBorder();
        MatrixStack m = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();

        double minX = border.getBoundWest();
        double maxX = border.getBoundEast();
        double minZ = border.getBoundNorth();
        double maxZ = border.getBoundSouth();
        double minY = mc.world.getBottomY();
        double maxY = mc.world.getTopY();

        Box northWall = new Box(minX, minY, minZ, maxX, maxY, minZ + 0.1);
        Box southWall = new Box(minX, minY, maxZ - 0.1, maxX, maxY, maxZ);
        Box westWall  = new Box(minX, minY, minZ, minX + 0.1, maxY, maxZ);
        Box eastWall  = new Box(maxX - 0.1, minY, minZ, maxX, maxY, maxZ);

        for (Box wall : new Box[]{northWall, southWall, westWall, eastWall}) {
            RenderUtil.drawFilledBox(m, wall, r, g, b, 0.1f);
            RenderUtil.drawESPBox(m, wall, r, g, b, a, 1.5f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showDistance.isEnabled() || mc.world == null || mc.player == null) return;
        WorldBorder border = mc.world.getWorldBorder();
        double dist = border.getDistanceToBorder(mc.player);
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        String label = String.format("Border: %.0fm", dist);
        ctx.drawTextWithShadow(mc.textRenderer, label, sw / 2 - mc.textRenderer.getWidth(label) / 2, 2, 0xFF4488FF);
    }
}
