package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.Hand;

/**
 * AutoFish - automatically casts the fishing rod and reels in when something is caught.
 *
 * Detection: watches for the bobber entity's Y velocity dropping (it dips when a fish bites).
 * Also listens for EntityVelocityUpdateS2CPacket on the bobber which signals a catch.
 */
public class AutoFish extends Module {

    private int castDelay = 0;
    private boolean waitingForCatch = false;
    private FishingBobberEntity lastBobber = null;
    private double lastBobberY = Double.MAX_VALUE;

    public AutoFish() {
        super("AutoFish", "Auto-casts rod and reels in when bobber dips", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        castDelay = 0;
        waitingForCatch = false;
        lastBobber = null;
        lastBobberY = Double.MAX_VALUE;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Must be holding fishing rod
        if (mc.player.getMainHandStack().getItem() != Items.FISHING_ROD
                && mc.player.getOffHandStack().getItem() != Items.FISHING_ROD) return;

        Hand rodHand = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD
                ? Hand.MAIN_HAND : Hand.OFF_HAND;

        // Cast delay after catching
        if (castDelay > 0) {
            castDelay--;
            return;
        }

        FishingBobberEntity bobber = mc.player.fishHook;

        if (bobber == null) {
            // Not fishing â€” cast the rod
            mc.interactionManager.interactItem(mc.player, rodHand);
            waitingForCatch = false;
            lastBobberY = Double.MAX_VALUE;
            lastBobber = null;
            return;
        }

        lastBobber = bobber;

        // Detect a bite: bobber suddenly moves downward
        double currentY = bobber.getY();
        if (lastBobberY != Double.MAX_VALUE) {
            double deltaY = currentY - lastBobberY;
            // A negative dip of more than 0.1 blocks indicates a fish biting
            if (deltaY < -0.1) {
                // Reel in
                mc.interactionManager.interactItem(mc.player, rodHand);
                castDelay = 20; // Wait 20 ticks before re-casting
                waitingForCatch = false;
                lastBobberY = Double.MAX_VALUE;
                return;
            }
        }
        lastBobberY = currentY;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt) {
            FishingBobberEntity bobber = mc.player.fishHook;
            if (bobber == null) return;

            // Check if velocity update is for our bobber
            if (pkt.getEntityId() != bobber.getId()) return;

            // Bobber velocity spike means fish caught
            double vy = pkt.getVelocityY() / 8000.0;
            if (vy < -0.1) {
                Hand rodHand = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD
                        ? Hand.MAIN_HAND : Hand.OFF_HAND;
                mc.interactionManager.interactItem(mc.player, rodHand);
                castDelay = 20;
                lastBobberY = Double.MAX_VALUE;
            }
        }
    }
}
