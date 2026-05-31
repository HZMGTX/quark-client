package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class SkyESP extends Module {

    private final BoolSetting showItems = register(new BoolSetting("ShowItems", "Also highlight item entities above you", false));

    public SkyESP() {
        super("SkyESP", "Renders ESP markers for entities that are above the player", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        double playerY = mc.player.getY();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.getY() <= playerY) continue;
            if (entity instanceof ItemEntity && !showItems.isEnabled()) continue;
            if (!(entity instanceof LivingEntity) && !(entity instanceof ItemEntity)) continue;

            float r, g, b;
            if (entity instanceof PlayerEntity) {
                r = 1f; g = 0.3f; b = 0.3f;
            } else if (entity instanceof ItemEntity) {
                r = 0.2f; g = 1f; b = 0.2f;
            } else {
                r = 0.8f; g = 0.8f; b = 0f;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Box box = entity.getBoundingBox().offset(ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());
            RenderUtil.drawESPBox(m, box, r, g, b, 0.9f, 1.2f);
        }
    }
}
