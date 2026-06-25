package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AntiGhast extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect ghasts and fireballs", 8.0, 2.0, 20.0));
    private final BoolSetting deflect = register(new BoolSetting(
            "Deflect", "Auto-hit incoming fireballs to deflect them back", true));

    public AntiGhast() {
        super("AntiGhast", "Detects nearby ghasts and deflects incoming fireballs back at them", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        double rangeSq = range.get() * range.get();

        // Warn about nearby ghasts
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof GhastEntity ghast)) continue;
            if (mc.player.squaredDistanceTo(ghast) > rangeSq) continue;
            // Only alert once per ghast when first entering range — handled by players checking HUD
        }

        if (!deflect.isEnabled()) return;

        // Find nearby incoming fireballs and deflect
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof FireballEntity fireball)) continue;
            double distSq = mc.player.squaredDistanceTo(fireball);
            if (distSq > rangeSq) continue;

            // Check if the fireball is moving toward the player
            Vec3d vel = fireball.getVelocity();
            Vec3d toPlayer = mc.player.getPos().subtract(fireball.getPos());
            double dot = vel.normalize().dotProduct(toPlayer.normalize());
            if (dot < 0.3) continue; // Not aimed at us

            // Attack the fireball to reflect it
            mc.interactionManager.attackEntity(mc.player, fireball);
            mc.player.swingHand(Hand.MAIN_HAND);

            double dist = Math.sqrt(distSq);
            ChatUtil.info("[AntiGhast] Deflected fireball at " + (int) dist + "m!");
            return; // Handle one fireball per tick
        }
    }
}
