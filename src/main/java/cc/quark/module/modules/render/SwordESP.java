package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.Box;

public class SwordESP extends Module {

    public SwordESP() {
        super("SwordESP", "Highlights players holding melee weapons with color-coded weapon type", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (e == mc.player) continue;

            ItemStack held = p.getMainHandStack();
            Item item = held.getItem();

            float r, g, b;
            if (item instanceof SwordItem sword) {
                ToolMaterial mat = sword.getMaterial();
                if (mat == ToolMaterials.NETHERITE)      { r = 0.6f; g = 0.2f; b = 0.6f; }
                else if (mat == ToolMaterials.DIAMOND)   { r = 0.2f; g = 0.9f; b = 1.0f; }
                else if (mat == ToolMaterials.GOLD)      { r = 1.0f; g = 0.85f; b = 0.0f; }
                else if (mat == ToolMaterials.IRON)      { r = 0.8f; g = 0.8f; b = 0.8f; }
                else                                     { r = 0.5f; g = 0.3f; b = 0.1f; }
            } else if (item instanceof AxeItem) {
                r = 1.0f; g = 0.5f; b = 0.2f;
            } else if (item instanceof TridentItem) {
                r = 0.2f; g = 0.7f; b = 1.0f;
            } else {
                continue;
            }

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ());
            RenderUtil.drawESPBox(m, box, r, g, b, 0.9f, 1.5f);
        }
    }
}
