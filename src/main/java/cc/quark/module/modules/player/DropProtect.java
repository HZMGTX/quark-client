package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

/**
 * DropProtect — prevents accidental item dropping by cancelling the drop-item
 * action packet before it is sent to the server.
 *
 * The player can hold a configurable modifier key (Ctrl / Shift) to temporarily
 * bypass the protection for intentional drops.
 */
public class DropProtect extends Module {

    private final BoolSetting allowCtrlDrop = register(new BoolSetting(
            "Allow Ctrl+Q", "Allow dropping when Ctrl is held (intentional drop)", true));

    private final BoolSetting protectAll = register(new BoolSetting(
            "Protect All", "Block ALL drop packets, including Ctrl+Q", false));

    private final BoolSetting notifyOnBlock = register(new BoolSetting(
            "Notify", "Warn in chat when a drop is blocked", false));

    public DropProtect() {
        super("DropProtect", "Prevents accidental item dropping", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;
        if (mc.player == null) return;

        PlayerActionC2SPacket.Action action = pkt.getAction();

        boolean isDrop = (action == PlayerActionC2SPacket.Action.DROP_ITEM
                || action == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS);

        if (!isDrop) return;

        // Determine whether to block this drop
        boolean block = true;

        if (!protectAll.isEnabled() && allowCtrlDrop.isEnabled()) {
            // If the player is holding CTRL, allow the drop through
            boolean ctrlHeld = org.lwjgl.glfw.GLFW.glfwGetKey(
                    mc.getWindow().getHandle(),
                    org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                    || org.lwjgl.glfw.GLFW.glfwGetKey(
                    mc.getWindow().getHandle(),
                    org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            if (ctrlHeld) block = false;
        }

        if (protectAll.isEnabled()) block = true;

        if (block) {
            event.cancel();
            if (notifyOnBlock.isEnabled()) {
                ItemStack held = mc.player.getMainHandStack();
                ChatUtil.warn("DropProtect: blocked drop of "
                        + (held.isEmpty() ? "air" : held.getItem().getName().getString()));
            }
        }
    }
}
