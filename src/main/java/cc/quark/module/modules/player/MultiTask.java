package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class MultiTask extends Module {

    private final BoolSetting allowMove = register(new BoolSetting(
            "Allow Move", "Allow WASD movement while inventory is open", true));
    private final BoolSetting allowJump = register(new BoolSetting(
            "Allow Jump", "Allow jumping while inventory is open", true));
    private final BoolSetting allowSneak = register(new BoolSetting(
            "Allow Sneak", "Allow sneaking while inventory is open", false));

    public MultiTask() {
        super("MultiTask", "Allows movement and actions while inventory screen is open", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?>)) return;

        if (allowMove.isEnabled()) {
            mc.player.input.movementForward  = 0f;
            mc.player.input.movementSideways = 0f;
            if (mc.options.forwardKey.isPressed())  mc.player.input.movementForward  =  1f;
            if (mc.options.backKey.isPressed())     mc.player.input.movementForward  = -1f;
            if (mc.options.leftKey.isPressed())     mc.player.input.movementSideways =  1f;
            if (mc.options.rightKey.isPressed())    mc.player.input.movementSideways = -1f;
        }
        if (allowJump.isEnabled() && mc.options.jumpKey.isPressed()) {
            mc.player.jump();
        }
        if (allowSneak.isEnabled() && mc.options.sneakKey.isPressed()) {
            mc.player.setSneaking(true);
        }
    }

    public static boolean isActive() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc == null) return false;
        cc.quark.Quark qc = cc.quark.Quark.getInstance();
        if (qc == null) return false;
        MultiTask mod = qc.getModuleManager().getModule(MultiTask.class);
        return mod != null && mod.isEnabled();
    }
}
