package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import org.lwjgl.glfw.GLFW;

public class InvProtect extends Module {

    private final BoolSetting blockQ = register(new BoolSetting("BlockQ", "Block Q key item drop", true));
    private final BoolSetting blockCtrlDrop = register(new BoolSetting("BlockCtrlDrop", "Block Ctrl+Q stack drop", true));
    private final BoolSetting showWarning = register(new BoolSetting("ShowWarning", "Show warning when blocked", true));

    public InvProtect() {
        super("InvProtect", "Prevents items from being accidentally dropped", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerActionC2SPacket packet)) return;
        if (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM
                || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
            event.cancel();
            if (showWarning.getValue()) ChatUtil.warn("Item drop blocked by InvProtect!");
        }
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (!blockQ.getValue()) return;
        if (event.getKey() == GLFW.GLFW_KEY_Q) {
            if (showWarning.getValue()) ChatUtil.warn("Q key drop blocked!");
        }
    }
}
