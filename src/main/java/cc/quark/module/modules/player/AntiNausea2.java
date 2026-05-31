package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiNausea2 extends Module {

    private final BoolSetting removeVignette = register(new BoolSetting(
            "RemoveVignette", "Also removes the nausea vignette overlay", true));

    public AntiNausea2() {
        super("AntiNausea2", "Removes nausea/confusion potion effect from visual rendering", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
    }

    public boolean shouldRemoveVignette() {
        return isEnabled() && removeVignette.isEnabled();
    }
}
