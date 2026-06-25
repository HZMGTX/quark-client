package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * FoodESP — highlights food item entities in the world with colored outlines,
 * optional fill, and optional tracers.
 */
public class FoodESP extends Module {

    private final IntSetting    range     = register(new IntSetting(   "Range",     "Detection range in blocks", 48, 4, 128));
    private final ColorSetting  color     = register(new ColorSetting( "Color",     "ESP outline color",         0xFF00FF88));
    private final DoubleSetting fillAlpha = register(new DoubleSetting("Fill Alpha","Fill box transparency",     0.15, 0.0, 1.0));
    private final BoolSetting   tracers   = register(new BoolSetting(  "Tracers",   "Draw tracer lines to food", false));
    private final BoolSetting   showLabel = register(new BoolSetting(  "Labels",    "Show food item name",       false));

    public FoodESP() {
        super("FoodESP", "Highlights food item drops in the world with colored outlines", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float td = event.getTickDelta();
        double maxRange = range.get();
        Vec3d from = mc.player.getCameraPosVec(td);

        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();
        float fa = (float) fillAlpha.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity itemEnt)) continue;
            if (mc.player.distanceTo(entity) > maxRange) continue;

            ItemStack stack = itemEnt.getStack();
            if (stack.isEmpty()) continue;

            // Check if the item has a FoodComponent (is edible)
            if (stack.get(DataComponentTypes.FOOD) == null) continue;

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Box box = entity.getBoundingBox().offset(ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, a, 1.5f);

            if (fa > 0) {
                RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
            }

            if (tracers.isEnabled()) {
                Vec3d center = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
                RenderUtil.drawLine3D(matrices, from, center, r, g, b, a * 0.7f, 1.0f);
            }
        }
    }
}
