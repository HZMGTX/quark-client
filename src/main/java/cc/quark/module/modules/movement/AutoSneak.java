package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class AutoSneak extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to sneak", "Always", "Always", "Moving", "Still"));
    private final BoolSetting sprintCancel = register(new BoolSetting(
            "Cancel Sprint", "Stop sprinting when auto-sneaking", true));

    public AutoSneak() {
        super("AutoSneak", "Automatically holds the sneak key based on movement state", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.input.sneaking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;

        boolean shouldSneak = switch (mode.get()) {
            case "Always"  -> true;
            case "Moving"  -> moving;
            case "Still"   -> !moving;
            default        -> false;
        };

        mc.player.input.sneaking = shouldSneak;
        if (shouldSneak && sprintCancel.isEnabled()) mc.player.setSprinting(false);
    }
}
