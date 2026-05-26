package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

/**
 * AutoFeed - keeps the client-side food level topped up below a threshold.
 */
public class AutoFeed extends Module {

    private final IntSetting target = register(new IntSetting("Target", "Food level to maintain", 20, 1, 20));

    public AutoFeed() {
        super("AutoFeed", "Keeps client food level topped up", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.getHungerManager().getFoodLevel() < target.get()) {
            mc.player.getHungerManager().setFoodLevel(target.get());
            mc.player.getHungerManager().setSaturationLevel(5.0f);
        }
    }
}
