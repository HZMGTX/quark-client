package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class NecroAura extends Module {

    private final BoolSetting enabled = register(new BoolSetting("Active", "Enable necromancy logic", true));

    public NecroAura() {
        super("NecroAura", "Revives fallen skeletons/zombies as allies", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || !enabled.isEnabled()) return;

        // Placeholder logic: scan for skeleton/zombie entities nearby
        // In a real implementation this would use a revival item or potion
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > 4.0) continue;

            if (entity instanceof ZombieEntity || entity instanceof AbstractSkeletonEntity) {
                // Simulate "rallying" the undead by looking at them
                Vec3d eyes = mc.player.getEyePos();
                Vec3d diff = entity.getPos().subtract(eyes).normalize();
                float yaw   = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
                float pitch = (float) -Math.toDegrees(Math.asin(diff.y));

                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
                // Placeholder: swing hand as a gesture
                mc.player.swingHand(Hand.OFF_HAND);
                break;
            }
        }
    }
}
