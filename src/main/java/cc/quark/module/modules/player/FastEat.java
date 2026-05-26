package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * FastEat - reduces food consumption time to near-instant.
 *
 * Packet mode: sends a stop-use packet immediately followed by a use packet
 * each tick, tricking the server into completing the eating animation faster.
 *
 * Timer mode: relies on the Timer module's speed multiplier (set Timer to high).
 */
public class FastEat extends Module {

    public enum Mode {
        PACKET, TIMER
    }

    private final EnumSetting<Mode> mode = register(new EnumSetting<>(
            "Mode", "Fast eating implementation method", Mode.PACKET));

    public FastEat() {
        super("FastEat", "Reduces food use time for faster eating", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mode.get() != Mode.PACKET) return;
        if (!mc.player.isUsingItem()) return;

        //? if mc >= "1.20.5" {
        if (!mc.player.getActiveItem().contains(net.minecraft.component.DataComponentTypes.FOOD)) return;
        int maxUseTicks = mc.player.getActiveItem().getMaxUseTime(mc.player);
        //?} else {
        /*if (!mc.player.getActiveItem().getItem().isFood()) return;
        int maxUseTicks = mc.player.getActiveItem().getItem().getMaxUseTime(mc.player.getActiveItem());*/
        //?}
        int usedTicks = mc.player.getItemUseTimeLeft();

        // If more than 5 ticks remaining, force finish by sending stop then restart
        if (usedTicks > 5) {
            // Send stop-use action
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN,
                    Direction.DOWN
            ));

            // Immediately resend use item to restart the use timer at server
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(
                    Hand.MAIN_HAND,
                    mc.player.getInventory().selectedSlot,
                    mc.player.getYaw(),
                    mc.player.getPitch()
            ));

            // Set client use time to 0 so it completes immediately next tick
            // We do this by forcefully setting the field via reflection or by
            // trusting the network round-trip will apply the item effect.
            // As a safe client-side approach we just drop the use time left counter.
            mc.player.stopUsingItem();
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
