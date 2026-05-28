package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class MultiTask extends Module {

    public MultiTask() {
        super("MultiTask", "Allows movement input while inventory is open", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?>)) return;
        mc.player.input.movementForward = 0f;
        mc.player.input.movementSideways = 0f;
        if (mc.options.forwardKey.isPressed()) mc.player.input.movementForward = 1f;
        if (mc.options.backKey.isPressed()) mc.player.input.movementForward = -1f;
        if (mc.options.leftKey.isPressed()) mc.player.input.movementSideways = 1f;
        if (mc.options.rightKey.isPressed()) mc.player.input.movementSideways = -1f;
        if (mc.options.jumpKey.isPressed()) mc.player.jump();
        if (mc.options.sneakKey.isPressed()) mc.player.setSneaking(true);
    }

    public static boolean isActive() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc == null) return false;
        cc.quark.Quark gc = cc.quark.Quark.getInstance();
        if (gc == null) return false;
        MultiTask mod = gc.getModuleManager().getModule(MultiTask.class);
        return mod != null && mod.isEnabled();
    }
}
