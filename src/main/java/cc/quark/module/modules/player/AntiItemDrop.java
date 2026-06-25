package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import org.lwjgl.glfw.GLFW;

public class AntiItemDrop extends Module {

    private final BoolSetting allowShiftDrop = register(new BoolSetting(
            "AllowShiftDrop", "Allow dropping the whole stack with Shift+Q", false));

    public AntiItemDrop() {
        super("AntiItemDrop", "Cancels the Q key item drop to prevent accidental item loss", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;

        boolean isSingleDrop = pkt.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM;
        boolean isStackDrop  = pkt.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS;

        if (isSingleDrop) {
            event.cancel();
            return;
        }

        if (isStackDrop && !allowShiftDrop.isEnabled()) {
            event.cancel();
        }
    }
}
