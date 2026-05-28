package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

public class ToolSaver extends Module {

    private final IntSetting minDurability = register(new IntSetting("Min Durability", "Cancel use when durability is this low", 10, 5, 100));
    private final TimerUtil warnTimer = new TimerUtil();

    public ToolSaver() {
        super("ToolSaver", "Prevents using tools below minimum durability", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof PlayerInteractBlockC2SPacket)) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || !held.isDamageable()) return;

        int remaining = held.getMaxDamage() - held.getDamage();
        if (remaining <= minDurability.get()) {
            event.cancel();
            if (warnTimer.hasReached(2000)) {
                NotificationOverlay.send("ToolSaver", "Tool too damaged to use! (" + remaining + " left)", NotificationOverlay.NotifType.WARNING);
                warnTimer.reset();
            }
        }
    }
}
