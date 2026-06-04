package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.Hand;

public class SilentSwing extends Module {

    private final BoolSetting main = register(new BoolSetting(
            "MainHand", "Suppress main hand arm swing animation", true));
    private final BoolSetting off = register(new BoolSetting(
            "OffHand", "Suppress off hand arm swing animation", true));

    public SilentSwing() {
        super("SilentSwing", "Swings arm silently without animation", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (main.isEnabled()) {
            mc.player.handSwingProgress = 0f;
            mc.player.lastHandSwingProgress = 0f;
        }

        if (off.isEnabled()) {
            // Suppress any pending off-hand swing by zeroing main swing state
            // (Fabric 1.21 does not expose a separate offHandSwingProgress field)
            mc.player.handSwingProgress = 0f;
            mc.player.lastHandSwingProgress = 0f;
        }
    }
}
