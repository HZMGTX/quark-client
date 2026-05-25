package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class Hitbox extends Module {

    private final BoolSetting  players   = register(new BoolSetting("Players",  "Show player hitboxes",   true));
    private final BoolSetting  mobs      = register(new BoolSetting("Mobs",     "Show mob hitboxes",      true));
    private final BoolSetting  other     = register(new BoolSetting("Other",    "Show all other entities",false));
    private final ColorSetting playerCol = register(new ColorSetting("Player Color","Player box color", 0xFF00FF00));
    private final ColorSetting mobCol    = register(new ColorSetting("Mob Color",   "Mob box color",    0xFFFF5555));

    public Hitbox() {
        super("Hitbox", "Renders accurate entity hitboxes in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float td = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;

            float r, g, b;
            if (entity instanceof PlayerEntity) {
                if (!players.isEnabled()) continue;
                r = playerCol.getRedF(); g = playerCol.getGreenF(); b = playerCol.getBlueF();
            } else if (entity instanceof LivingEntity) {
                if (!mobs.isEnabled()) continue;
                r = mobCol.getRedF(); g = mobCol.getGreenF(); b = mobCol.getBlueF();
            } else {
                if (!other.isEnabled()) continue;
                r = 0.8f; g = 0.8f; b = 0.8f;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Box box = entity.getBoundingBox().offset(ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            RenderUtil.drawESPBox(matrices, box, r, g, b, 0.7f, 1.0f);
        }
    }
}
