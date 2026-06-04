package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoNotch extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Target range in blocks", 3.5, 1.0, 8.0));

    private float targetYaw   = 0f;
    private float targetPitch = 0f;
    private boolean hasTarget = false;

    public AutoNotch() {
        super("AutoNotch", "Aims at enemy eyes like Notch's PvP", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        hasTarget = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) {
            hasTarget = false;
            return;
        }

        LivingEntity target = findNearest();
        if (target == null) {
            hasTarget = false;
            return;
        }

        Vec3d eyes   = mc.player.getEyePos();
        Vec3d tEyes  = target.getEyePos();

        double dx = tEyes.x - eyes.x;
        double dy = tEyes.y - eyes.y;
        double dz = tEyes.z - eyes.z;

        targetYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        targetPitch = MathHelper.clamp(targetPitch, -90f, 90f);
        hasTarget   = true;
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (!hasTarget) return;
        event.setYaw(targetYaw);
        event.setPitch(targetPitch);
    }

    private LivingEntity findNearest() {
        LivingEntity best = null;
        double bestDist   = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best     = (LivingEntity) e;
            }
        }
        return best;
    }
}
