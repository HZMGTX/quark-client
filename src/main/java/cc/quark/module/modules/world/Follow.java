package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Follow extends Module {

    private final ModeSetting  target   = register(new ModeSetting("Target", "What entity type to follow", "Nearest Player", "Nearest Player", "Nearest Mob", "Nearest Entity"));
    private final DoubleSetting stopDist= register(new DoubleSetting("Stop Distance","Stop following when this close", 3.0, 1.0, 10.0));

    public Follow() {
        super("Follow", "Automatically follows the nearest target entity", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Entity tgt = findTarget();
        if (tgt == null) { mc.player.input.movementForward = 0; return; }

        double dist = mc.player.distanceTo(tgt);
        if (dist <= stopDist.get()) { mc.player.input.movementForward = 0; return; }

        Vec3d dir = tgt.getPos().subtract(mc.player.getPos()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        mc.player.setYaw(yaw);
        mc.player.input.movementForward = 1.0f;
    }

    private Entity findTarget() {
        Entity closest = null;
        double best = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            boolean matches = switch (target.get()) {
                case "Nearest Player" -> e instanceof PlayerEntity;
                case "Nearest Mob"   -> e instanceof MobEntity;
                default              -> e instanceof LivingEntity;
            };
            if (!matches) continue;
            double d = mc.player.distanceTo(e);
            if (d < best) { best = d; closest = e; }
        }
        return closest;
    }
}
