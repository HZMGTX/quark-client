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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.math.Vec3d;

/**
 * EntityTracers2 — enhanced entity tracers with distinct colors per entity type,
 * configurable line width, and optional distance-based fade.
 */
public class EntityTracers2 extends Module {

    // Per-type toggles
    private final BoolSetting players  = register(new BoolSetting("Players",  "Trace players",         true));
    private final BoolSetting hostiles = register(new BoolSetting("Hostiles", "Trace hostile mobs",    true));
    private final BoolSetting passives = register(new BoolSetting("Passives", "Trace passive animals", false));
    private final BoolSetting items    = register(new BoolSetting("Items",    "Trace dropped items",   false));
    private final BoolSetting arrows   = register(new BoolSetting("Arrows",   "Trace projectiles",     false));

    // Per-type colors
    private final ColorSetting playerColor  = register(new ColorSetting("Player Color",  "Tracer color for players",      0xFFFF4040));
    private final ColorSetting hostileColor = register(new ColorSetting("Hostile Color", "Tracer color for hostiles",     0xFFFF8800));
    private final ColorSetting passiveColor = register(new ColorSetting("Passive Color", "Tracer color for passives",     0xFF40FF40));
    private final ColorSetting itemColor    = register(new ColorSetting("Item Color",    "Tracer color for item drops",   0xFFFFFF40));
    private final ColorSetting arrowColor   = register(new ColorSetting("Arrow Color",   "Tracer color for projectiles",  0xFF40FFFF));

    // Options
    private final DoubleSetting maxRange  = register(new DoubleSetting("Max Range",   "Maximum tracer distance",  128, 8, 512));
    private final DoubleSetting lineWidth = register(new DoubleSetting("Line Width",  "Tracer line thickness",    1.0, 0.5, 4.0));
    private final BoolSetting   fadeAlpha = register(new BoolSetting  ("Fade Alpha",  "Fade line alpha with distance", true));
    private final BoolSetting   showDist  = register(new BoolSetting  ("Show Dist",   "Show distance labels",     false));

    public EntityTracers2() {
        super("EntityTracers2", "Enhanced entity tracers with per-type colors and distance fade", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        Vec3d origin = mc.player.getEyePos();
        float td = event.getTickDelta();
        double maxRng = maxRange.get();
        float lw = (float) lineWidth.get();

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
            } else if (entity instanceof HostileEntity) {
                if (!hostiles.isEnabled()) continue;
                r = hostileColor.getRedF();
                g = hostileColor.getGreenF();
                b = hostileColor.getBlueF();
                a = hostileColor.getAlphaF();
            } else if (entity instanceof AnimalEntity || entity instanceof ArmorStandEntity) {
                if (!passives.isEnabled()) continue;
                r = passiveColor.getRedF();
                g = passiveColor.getGreenF();
                b = passiveColor.getBlueF();
                a = passiveColor.getAlphaF();
            } else if (entity instanceof ItemEntity) {
                if (!items.isEnabled()) continue;
                r = itemColor.getRedF();
                g = itemColor.getGreenF();
                b = itemColor.getBlueF();
                a = itemColor.getAlphaF();
            } else if (entity instanceof AbstractArrowEntity) {
                if (!arrows.isEnabled()) continue;
                r = arrowColor.getRedF();
                g = arrowColor.getGreenF();
                b = arrowColor.getBlueF();
                a = arrowColor.getAlphaF();
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;

            Vec3d target = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
            double dist = origin.distanceTo(target);
            if (dist > maxRng) continue;

            // Apply distance fade
            float fa = a;
            if (fadeAlpha.isEnabled()) {
                fa = a * (float) Math.max(0.1, 1.0 - dist / maxRng);
            }

            RenderUtil.drawLine3D(event.getMatrixStack(), origin, target, r, g, b, fa, lw);
        }
    }
}
