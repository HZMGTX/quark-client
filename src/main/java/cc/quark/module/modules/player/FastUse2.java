package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

/**
 * FastUse2 - repeatedly fires item use to speed up consumption/usage.
 */
public class FastUse2 extends Module {

    private final IntSetting speed = register(new IntSetting("Speed", "Use actions per tick", 3, 1, 10));

    public FastUse2() {
        super("FastUse2", "Speeds up item usage", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!mc.player.isUsingItem()) return;
        for (int i = 0; i < speed.get(); i++) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
