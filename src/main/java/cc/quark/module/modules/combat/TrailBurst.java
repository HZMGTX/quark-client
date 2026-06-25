package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * TrailBurst - watches arrows that the user has shot and, when a tracked arrow
 * is predicted to be about to hit a target, fires additional projectiles
 * (snowballs or more arrows) to burst-damage the area.
 *
 * <p>The module tracks in-flight arrows owned by the local player and triggers
 * a burst when the arrow comes within the configured proximity of an enemy.
 */
public class TrailBurst extends Module {

    private final DoubleSetting burstRange = register(new DoubleSetting(
            "Burst Range", "Arrow must be within this distance of a target to trigger burst (blocks)", 4.0, 1.0, 10.0));

    private final IntSetting burstCount = register(new IntSetting(
            "Burst Count", "Number of extra projectiles to fire in the burst", 3, 1, 8));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown", "Minimum time between bursts (ms)", 2000, 500, 10000));

    private final BoolSetting useSnowballs = register(new BoolSetting(
            "Use Snowballs", "Use snowballs for burst (otherwise arrows)", true));

    private final TimerUtil burstTimer = new TimerUtil();
    private int previousSlot = -1;

    public TrailBurst() {
        super("TrailBurst", "Fires burst projectiles when an arrow nears its target", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        burstTimer.reset();
        previousSlot = -1;
    }

    @Override
    public void onDisable() {
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            previousSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!burstTimer.hasReached(cooldownMs.get())) return;

        // Find arrows shot by us
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ArrowEntity arrow)) continue;
            if (arrow.getOwner() != mc.player) continue;

            // Check if this arrow is near an enemy
            Vec3d arrowPos = arrow.getPos();
            for (Entity target : mc.world.getEntities()) {
                if (target == mc.player) continue;
                if (!(target instanceof PlayerEntity p)) continue;
                if (p.isDead() || p.getHealth() <= 0f) continue;

                double dist = arrowPos.distanceTo(target.getPos());
                if (dist <= burstRange.get()) {
                    fireBurst();
                    burstTimer.reset();
                    return;
                }
            }
        }
    }

    private void fireBurst() {
        int burstSlot = findProjectileSlot();
        if (burstSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = burstSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(burstSlot));

        int count = burstCount.get();
        for (int i = 0; i < count; i++) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }

        mc.player.getInventory().selectedSlot = previousSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        previousSlot = -1;
    }

    private int findProjectileSlot() {
        if (useSnowballs.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.SNOWBALL) return i;
            }
        }
        // Fallback to arrows or bow
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ARROW) return i;
        }
        return -1;
    }
}
