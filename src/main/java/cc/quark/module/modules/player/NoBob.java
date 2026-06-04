package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoBob extends Module {

    private final BoolSetting walk = register(new BoolSetting(
            "Walk", "Remove camera bobbing while walking", true));
    private final BoolSetting hand = register(new BoolSetting(
            "Hand", "Remove hand bobbing animation", true));

    public NoBob() {
        super("NoBob", "Removes camera bobbing while walking", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        applyBobState();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.bobView().setValue(true);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        applyBobState();
    }

    private void applyBobState() {
        if (mc.options == null || mc.player == null) return;

        if (walk.isEnabled()) {
            mc.options.bobView().setValue(false);
        }

        if (hand.isEnabled() && mc.player != null) {
            mc.player.handSwingProgress = 0f;
            mc.player.lastHandSwingProgress = 0f;
        }
    }
}
