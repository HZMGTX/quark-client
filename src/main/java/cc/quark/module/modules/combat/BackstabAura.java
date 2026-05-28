package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class BackstabAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 4.0, 1.0, 6.0));
    private final BoolSetting onlyPlayers = register(new BoolSetting("Only Players", "Only target other players", true));

    public BackstabAura() {
        super("BackstabAura", "Attacks enemies from behind", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        LivingEntity target = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < bestDist) {
                bestDist = dist;
                target = living;
            }
        }

        if (target == null || !isPlayerBehindEntity(target)) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isPlayerBehindEntity(LivingEntity entity) {
        float entityYawRad = (float) Math.toRadians(entity.getYaw());
        Vec3d entityFacing = new Vec3d(-Math.sin(entityYawRad), 0, Math.cos(entityYawRad)).normalize();

        Vec3d toPlayer = mc.player.getPos().subtract(entity.getPos());
        toPlayer = new Vec3d(toPlayer.x, 0, toPlayer.z).normalize();

        double dot = entityFacing.dotProduct(toPlayer);
        double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));

        return angle < 45.0;
    }
}
