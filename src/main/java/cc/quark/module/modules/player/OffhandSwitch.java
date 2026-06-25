package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class OffhandSwitch extends Module {

    private final BoolSetting autoSwapEmpty = register(new BoolSetting(
            "AutoSwap", "Auto-swap when offhand becomes empty", true));

    public OffhandSwitch() {
        super("OffhandSwitch", "Swaps main hand and offhand items on key press", Category.PLAYER);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        if (event.getKeyCode() == GLFW.GLFW_KEY_F) {
            swapHands();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoSwapEmpty.isEnabled()) return;
        if (mc.player == null) return;

        ItemStack offhand = mc.player.getOffHandStack();
        ItemStack mainhand = mc.player.getMainHandStack();

        if (offhand.isEmpty() && !mainhand.isEmpty()) {
            swapHands();
        }
    }

    private void swapHands() {
        if (mc.player == null) return;
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                        BlockPos.ORIGIN,
                        Direction.DOWN));
    }
}
