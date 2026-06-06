package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class InventoryWalk extends Module {
    public InventoryWalk() { super("InventoryWalk", "Move while inventory screens are open", Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || !(mc.currentScreen instanceof HandledScreen<?>)) return;
        float forward = 0, side = 0;
        if (mc.options.forwardKey.isPressed()) forward = 1f;
        if (mc.options.backKey.isPressed()) forward = -1f;
        if (mc.options.leftKey.isPressed()) side = 1f;
        if (mc.options.rightKey.isPressed()) side = -1f;
        mc.player.input.movementForward = forward;
        mc.player.input.movementSideways = side;
    }
}
