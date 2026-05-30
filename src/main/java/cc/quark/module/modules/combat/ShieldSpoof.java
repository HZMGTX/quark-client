package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

/**
 * ShieldSpoof — intercepts outgoing interaction packets and spoofs shield-use
 * packets so the server thinks the player is blocking even without pressing use.
 */
public class ShieldSpoof extends Module {

    private final BoolSetting autoBlock  = register(new BoolSetting("AutoBlock",  "Automatically send shield block packets", true));
    private final BoolSetting spoofSneak = register(new BoolSetting("SpoofSneak", "Also spoof sneak state when blocking",    false));

    public ShieldSpoof() {
        super("ShieldSpoof", "Spoofs shield-use packets so the server registers a block without pressing use", Category.COMBAT);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!autoBlock.isEnabled()) return;
        if (mc.player == null) return;

        boolean holdingShield = mc.player.getMainHandStack().isOf(Items.SHIELD)
                || mc.player.getOffHandStack().isOf(Items.SHIELD);
        if (!holdingShield) return;

        boolean sneak = mc.player.isSneaking() || spoofSneak.isEnabled();
        if (!sneak) return;

        var pkt = event.getPacket();

        // Intercept USE_ITEM action or interact-item packets and ensure shield use is registered
        if (pkt instanceof PlayerActionC2SPacket actionPkt) {
            if (actionPkt.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
                // Player released use — cancel to keep blocking
                event.cancel();
            }
        }

        // Inject a shield interact packet alongside normal packets
        if (pkt instanceof PlayerInteractItemC2SPacket) {
            Hand shieldHand = mc.player.getOffHandStack().isOf(Items.SHIELD) ? Hand.OFF_HAND : Hand.MAIN_HAND;
            // Send a duplicate interact-item packet for the shield hand
            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(
                        new PlayerInteractItemC2SPacket(shieldHand, 0, mc.player.getYaw(), mc.player.getPitch()));
            }
        }
    }
}
