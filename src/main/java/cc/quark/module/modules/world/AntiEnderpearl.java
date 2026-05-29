package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public class AntiEnderpearl extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to handle ender pearl use",
            "Cancel", "Cancel", "Delay", "Spoof"));

    private final TimerUtil delayTimer = new TimerUtil();
    private PlayerInteractItemC2SPacket delayed = null;

    public AntiEnderpearl() {
        super("AntiEnderpearl", "Cancels, delays, or spoofs ender pearl teleport packets", Category.WORLD);
    }

    @Override
    public void onEnable() {
        delayed = null;
        delayTimer.reset();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof PlayerInteractItemC2SPacket)) return;

        var stack = mc.player.getMainHandStack();
        if (stack.isEmpty() || stack.getItem() != Items.ENDER_PEARL) return;

        String m = mode.get();
        if (m.equals("Cancel")) {
            event.cancel();
        } else if (m.equals("Delay")) {
            if (!delayTimer.hasReached(3000)) {
                event.cancel();
            } else {
                delayTimer.reset();
            }
        }
        // Spoof: let the packet through but do nothing extra (placeholder for rotation spoofing)
    }
}
