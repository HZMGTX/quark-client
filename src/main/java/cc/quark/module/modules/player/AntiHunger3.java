package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * AntiHunger3 - reduces hunger drain by preventing sprinting (the main source
 * of food exhaustion). Full saturation-drain suppression requires a mixin into
 * PlayerHungerManager; this module provides the static flag and handles
 * the cancel-sprinting side on the client tick.
 */
public class AntiHunger3 extends Module {

    private final BoolSetting cancelSprinting = register(new BoolSetting(
            "CancelSprinting", "Stop sprinting to reduce hunger drain", true));

    /**
     * Static flag queried by a mixin into ServerPlayNetworkHandler /
     * ClientPlayerEntity to suppress saturation exhaustion packets.
     */
    public static boolean suppressHunger = false;

    public AntiHunger3() {
        super("AntiHunger3", "Prevents hunger from depleting", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        suppressHunger = true;
    }

    @Override
    public void onDisable() {
        suppressHunger = false;
        if (mc.player != null) {
            mc.options.sprintKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        suppressHunger = true;

        if (cancelSprinting.isEnabled() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
    }
}
