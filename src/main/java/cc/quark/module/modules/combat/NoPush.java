package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * NoPush - prevents being pushed by other players and mobs by cancelling
 * unwanted horizontal velocity impulses caused by entity collisions.
 *
 * On every tick, if the player's horizontal speed exceeds their sprinting
 * threshold and they are being nudged by an entity, the extra velocity
 * is stripped away.
 */
public class NoPush extends Module {

    private final BoolSetting players = register(new BoolSetting(
            "Players", "Block pushes from other players", true));

    private final BoolSetting mobs = register(new BoolSetting(
            "Mobs", "Block pushes from mobs / entities", true));

    private final BoolSetting keepY = register(new BoolSetting(
            "Keep Y", "Also cancel vertical push from entities", false));

    public NoPush() {
        super("NoPush", "Prevents being pushed by other players/mobs", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean playerNearby = false;
        boolean mobNearby    = false;

        double checkRange = 1.2; // vanilla push range
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || e.isRemoved()) continue;
            if (mc.player.distanceTo(e) > checkRange) continue;

            if (e instanceof net.minecraft.entity.player.PlayerEntity) {
                playerNearby = true;
            } else if (e instanceof LivingEntity) {
                mobNearby = true;
            }
        }

        boolean shouldCancel = (players.isEnabled() && playerNearby)
                || (mobs.isEnabled() && mobNearby);

        if (!shouldCancel) return;

        Vec3d vel = mc.player.getVelocity();
        double newY = keepY.isEnabled() ? 0.0 : vel.y;
        mc.player.setVelocity(0.0, newY, 0.0);
    }
}
