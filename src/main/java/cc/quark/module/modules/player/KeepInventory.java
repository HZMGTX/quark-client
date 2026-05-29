package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.DropItemC2SPacket;

public class KeepInventory extends Module {

    private final BoolSetting onlyOnDeath = register(new BoolSetting(
            "Only On Death", "Only block item drops when player is dead", true));
    private final BoolSetting cancelAll = register(new BoolSetting(
            "Cancel All Drops", "Cancel all drop packets regardless of death state", false));

    public KeepInventory() {
        super("KeepInventory", "Cancels item-drop packets to prevent losing inventory on death", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof DropItemC2SPacket)) return;

        if (cancelAll.isEnabled()) {
            event.cancel();
            return;
        }

        if (onlyOnDeath.isEnabled() && (mc.player.isDead() || mc.player.getHealth() <= 0f)) {
            event.cancel();
        }
    }
}
