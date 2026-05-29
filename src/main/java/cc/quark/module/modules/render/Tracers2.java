package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Tracers2 - combined tracer module for all entity types with per-type settings
 * and color controls.
 */
public class Tracers2 extends Module {

    private final BoolSetting players  = register(new BoolSetting("Players",  "Draw tracers to players",      true));
    private final BoolSetting mobs     = register(new BoolSetting("Mobs",     "Draw tracers to passive mobs", false));
    private final BoolSetting items    = register(new BoolSetting("Items",    "Draw tracers to item drops",   false));
    private final BoolSetting hostiles = register(new BoolSetting("Hostiles", "Draw tracers to hostile mobs", true));

    private final ColorSetting playerColor  = register(new ColorSetting("PlayerColor",  "Player tracer color",  0xFFFF4040));
    private final ColorSetting mobColor     = register(new ColorSetting("MobColor",     "Mob tracer color",     0xFFFFFF40));
    private final ColorSetting itemColor    = register(new ColorSetting("ItemColor",    "Item tracer color",    0xFF40FF40));
    private final ColorSetting hostileColor = register(new ColorSetting("HostileColor", "Hostile tracer color", 0xFFFF8000));

    private final DoubleSetting maxRange = register(new DoubleSetting(
            "Max Range", "Maximum tracer distance in blocks", 64, 8, 256));

    public Tracers2() {
        super("Tracers2", "Combined tracer lines to all entity types", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        Vec3d origin = mc.player.getEyePos();
        float td = event.getTickDelta();
        double maxRng = maxRange.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isInvisible()) continue;

            float r, g, b, a;

            if (entity instanceof PlayerEntity) {
                if (!players.isEnabled()) continue;
                r = playerColor.getRedF();
                g = playerColor.getGreenF();
                b = playerColor.getBlueF();
                a = playerColor.getAlphaF();
            } else if (entity instanceof ItemEntity) {
                if (!items.isEnabled()) continue;
                r = itemColor.getRedF();
                g = itemColor.getGreenF();
                b = itemColor.getBlueF();
                a = itemColor.getAlphaF();
            } else if (entity instanceof HostileEntity) {
                if (!hostiles.isEnabled()) continue;
                r = hostileColor.getRedF();
                g = hostileColor.getGreenF();
                b = hostileColor.getBlueF();
                a = hostileColor.getAlphaF();
            } else if (entity instanceof MobEntity) {
                if (!mobs.isEnabled()) continue;
                r = mobColor.getRedF();
                g = mobColor.getGreenF();
                b = mobColor.getBlueF();
                a = mobColor.getAlphaF();
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;

            Vec3d target = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
            if (origin.distanceTo(target) > maxRng) continue;

            RenderUtil.drawLine3D(event.getMatrixStack(), origin, target, r, g, b, a, 1.0f);
        }
    }
}
