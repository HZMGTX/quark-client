package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.BlockPos;

/**
 * Sneak - keeps the player sneaking using one of three modes.
 *
 * Toggle: flips the sneak key state each tick.
 * Hold:   continuously sets the sneaking flag each tick.
 * Packet: sends PRESS_SHIFT_KEY packets to the server without visual sneak.
 *
 * SafeWalk: prevents the player from walking off edges while sneaking.
 */
public class Sneak extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Sneak implementation mode", "Hold",
            "Toggle", "Hold", "Packet"));

    private final BoolSetting safeWalk = register(new BoolSetting(
            "Safe Walk", "Sneak on edges to avoid falling", true));

    private boolean packetSent = false;
    private boolean toggled    = false;

    public Sneak() {
        super("Sneak", "Automatically keeps the player sneaking", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        packetSent = false;
        toggled    = false;
        if (mode.is("Packet")) sendSneak(true);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.setSneaking(false);
        mc.options.sneakKey.setPressed(false);
        if (mode.is("Packet") && packetSent) {
            sendSneak(false);
            packetSent = false;
        }
        toggled = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // SafeWalk: enable vanilla sneak when near an edge
        if (safeWalk.isEnabled()) {
            BlockPos below = mc.player.getBlockPos().down();
            boolean onGround = mc.player.isOnGround();
            if (onGround && mc.world != null && mc.world.getBlockState(below).isAir()) {
                mc.player.setSneaking(true);
                return;
            }
        }

        switch (mode.get()) {
            case "Toggle" -> {
                toggled = !toggled;
                mc.player.setSneaking(toggled);
                mc.options.sneakKey.setPressed(toggled);
            }
            case "Hold" -> {
                mc.player.setSneaking(true);
                mc.options.sneakKey.setPressed(true);
            }
            case "Packet" -> {
                if (!packetSent) {
                    sendSneak(true);
                    packetSent = true;
                }
            }
        }
    }

    private void sendSneak(boolean press) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        ClientCommandC2SPacket.Mode m = press
                ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, m));
    }
}
