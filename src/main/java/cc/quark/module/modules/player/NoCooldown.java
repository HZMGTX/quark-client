package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * NoCooldown - removes the attack cooldown so swings always deal full damage.
 */
public class NoCooldown extends Module {

    private final BoolSetting items = register(new BoolSetting("Items", "Also ignore item cooldowns", true));

    public NoCooldown() {
        super("NoCooldown", "Removes attack and item cooldowns", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Cooldown reset enforced via interaction mixin.
    }
}
