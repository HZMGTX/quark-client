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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * ESP2 - all-in-one ESP module with per-type toggles, optional tracers,
 * filled boxes, and configurable colours.
 */
public class ESP2 extends Module {

    private final BoolSetting players  = register(new BoolSetting("Players",  "ESP for players",        true));
    private final BoolSetting mobs     = register(new BoolSetting("Mobs",     "ESP for neutral mobs",   false));
    private final BoolSetting hostiles = register(new BoolSetting("Hostiles", "ESP for hostile mobs",   true));
    private final BoolSetting passives = register(new BoolSetting("Passives", "ESP for passive animals",false));

    private final BoolSetting tracers  = register(new BoolSetting("Tracers",  "Draw tracer lines",      true));
    private final BoolSetting boxes    = register(new BoolSetting("Boxes",    "Draw ESP outline boxes", true));
    private final BoolSetting filled   = register(new BoolSetting("Filled",   "Fill boxes",             true));

    private final ColorSetting playerColor = register(new ColorSetting("PlayerColor", "Player color", 0xFFFF3030));
    private final ColorSetting mobColor    = register(new ColorSetting("MobColor",    "Mob color",    0xFFFFAA00));

    private final DoubleSetting range     = register(new DoubleSetting("Range",      "Max ESP range",           128, 16, 512));
    private final DoubleSetting fillAlpha = register(new DoubleSetting("Fill Alpha", "Fill transparency 0-255",  30,  0, 255));

    public ESP2() {
        super("ESP2", "All-in-one ESP with per-type settings, tracers, and filled boxes", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        float td = event.getTickDelta();
        double maxRng = range.get();
        float fa = (float)(fillAlpha.get() / 255.0);
        Vec3d origin = mc.player.getEyePos();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.isInvisible()) continue;

            float r, g, b;

            if (entity instanceof PlayerEntity) {
                if (!players.isEnabled()) continue;
                r = playerColor.getRedF(); g = playerColor.getGreenF(); b = playerColor.getBlueF();
            } else if (entity instanceof HostileEntity) {
                if (!hostiles.isEnabled()) continue;
                r = mobColor.getRedF(); g = mobColor.getGreenF(); b = mobColor.getBlueF();
            } else if (entity instanceof AnimalEntity) {
                if (!passives.isEnabled()) continue;
                r = 0.4f; g = 1.0f; b = 0.4f;
            } else if (entity instanceof MobEntity) {
                if (!mobs.isEnabled()) continue;
                r = mobColor.getRedF(); g = mobColor.getGreenF(); b = mobColor.getBlueF();
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;

            Vec3d center = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
            if (origin.distanceTo(center) > maxRng) continue;

            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            if (boxes.isEnabled()) {
                RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            }
            if (filled.isEnabled()) {
                RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, fa);
            }
            if (tracers.isEnabled()) {
                RenderUtil.drawLine3D(event.getMatrixStack(), origin, center, r, g, b, 0.7f, 1.0f);
            }
        }
    }
}
