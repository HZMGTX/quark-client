package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.Hand;

/**
 * AutoBridge - automatically places blocks beneath the player to bridge forward.
 */
public class AutoBridge extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Bridge mode", "Normal", "Normal", "God"));

    public AutoBridge() {
        super("AutoBridge", "Automatically bridges with held blocks", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getMainHandStack().isEmpty()) return;
        if (mode.is("God") && !mc.player.isOnGround()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
