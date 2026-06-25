package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class EntityGlow extends Module {

    private final ModeSetting target = register(new ModeSetting("Target", "Which entity types to outline", "Players", "Players", "Mobs", "All"));

    public EntityGlow() {
        super("EntityGlow", "Applies an outline glow effect to specific entity types", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        String mode = target.get();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (e == mc.player) continue;

            float r, g, b;
            boolean isPlayer = e instanceof PlayerEntity;
            boolean isMob    = e instanceof MobEntity;

            if ("Players".equals(mode) && !isPlayer) continue;
            if ("Mobs".equals(mode)    && !isMob)    continue;

            if (isPlayer)      { r = 1.0f; g = 0.2f; b = 0.2f; }
            else if (isMob)    { r = 1.0f; g = 0.5f; b = 0.0f; }
            else               { r = 0.5f; g = 1.0f; b = 0.5f; }

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            Box box = e.getBoundingBox().offset(ex - e.getX(), ey - e.getY(), ez - e.getZ()).expand(0.05);
            RenderUtil.drawESPBox(m, box, r, g, b, 0.85f, 2.5f);
        }
    }
}
