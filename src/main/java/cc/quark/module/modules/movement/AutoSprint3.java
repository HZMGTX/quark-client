package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

/**
 * AutoSprint3 - always-on sprint with anti-detection packet logic.
 *
 * Vanilla auto-sprint sends a SPRINTING packet every time the sprint state
 * changes, which NCP and similar anti-cheats track for consistency.  This
 * implementation batches the start-sprint packet through a configurable delay
 * and suppresses redundant stop-sprint packets so the cadence looks more like
 * a human who simply never releases the sprint key.
 */
public class AutoSprint3 extends Module {

    private final BoolSetting omniDirectional = register(new BoolSetting(
            "Omni Directional", "Sprint in any movement direction, not just forward", false));

    private final BoolSetting stopOnHit = register(new BoolSetting(
            "Stop On Hit", "Pause sprinting while taking damage (reduces NCP flags)", true));

    private final BoolSetting stopOnEat = register(new BoolSetting(
            "Stop On Eat", "Stop sprinting while eating or drinking", true));

    private final IntSetting packetDelay = register(new IntSetting(
            "Packet Delay", "Ticks before sending the sprint start packet (0 = instant)", 1, 0, 5));

    private int delayCounter = 0;
    private boolean pendingSprint = false;

    public AutoSprint3() {
        super("AutoSprint3", "Always-on sprint with anti-detection packet logic", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        delayCounter = 0;
        pendingSprint = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Stop conditions
        if (stopOnHit.isEnabled() && mc.player.hurtTime > 0) {
            if (mc.player.isSprinting()) mc.player.setSprinting(false);
            pendingSprint = false;
            delayCounter = 0;
            return;
        }
        if (stopOnEat.isEnabled() && mc.player.isUsingItem()) {
            if (mc.player.isSprinting()) mc.player.setSprinting(false);
            pendingSprint = false;
            delayCounter = 0;
            return;
        }
        if (mc.player.isSneaking()) return;

        // Determine if the player is moving in a direction eligible for sprint
        boolean moving = mc.player.input.movementForward > 0;
        if (omniDirectional.isEnabled()) {
            moving = mc.player.input.movementForward != 0
                    || mc.player.input.movementSideways != 0;
        }

        if (!moving) {
            pendingSprint = false;
            delayCounter = 0;
            return;
        }

        if (mc.player.isSprinting()) {
            pendingSprint = false;
            delayCounter = 0;
            return;
        }

        // Queue the sprint packet
        if (!pendingSprint) {
            pendingSprint = true;
            delayCounter = 0;
        }

        delayCounter++;
        if (delayCounter >= packetDelay.get()) {
            mc.player.setSprinting(true);
            // Send explicit packet for servers that require it
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(mc.player,
                                ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
            pendingSprint = false;
            delayCounter = 0;
        }
    }
}
