package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoHunger extends Module {

    private final BoolSetting keepSaturation = register(new BoolSetting(
            "Keep Saturation", "Also keep saturation at max level", true));

    public NoHunger() {
        super("NoHunger", "Prevents hunger and saturation loss", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        applyFood();
    }

    @EventHandler
    public void onTick(EventTick event) {
        applyFood();
    }

    private void applyFood() {
        if (mc.player == null) return;
        var hunger = mc.player.getHungerManager();
        if (hunger.getFoodLevel() < 20) {
            hunger.setFoodLevel(20);
        }
        if (keepSaturation.isEnabled() && hunger.getSaturationLevel() < 5.0f) {
            hunger.setSaturationLevel(5.0f);
        }
    }
}
