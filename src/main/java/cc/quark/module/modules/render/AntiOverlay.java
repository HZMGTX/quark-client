package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiOverlay extends Module {

    private final BoolSetting fire    = register(new BoolSetting("Fire",    "Remove fire overlay",         true));
    private final BoolSetting water   = register(new BoolSetting("Water",   "Suppress underwater overlay", true));
    private final BoolSetting pumpkin = register(new BoolSetting("Pumpkin", "Remove pumpkin head overlay", false));

    public AntiOverlay() {
        super("AntiOverlay", "Removes fire, water, and pumpkin head overlays from the screen", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (fire.isEnabled()) {
            // Keep fireImmuneTicks high to suppress the fire overlay render check
            if (mc.player.isOnFire()) {
                mc.player.setFireTicks(0);
            }
        }

        if (water.isEnabled()) {
            // Keep air at max to suppress the drowning/water overlay
            if (mc.player.isSubmergedInWater()) {
                mc.player.setAir(mc.player.getMaxAir());
            }
        }

        if (pumpkin.isEnabled()) {
            // Pumpkin overlay is rendered when wearing a carved pumpkin as helmet.
            // We cancel by removing blindness-like overlay; actual pumpkin removal
            // requires a mixin. Here we also remove darkness/blindness as a side effect.
            if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
                mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            }
            if (mc.player.hasStatusEffect(StatusEffects.DARKNESS)) {
                mc.player.removeStatusEffect(StatusEffects.DARKNESS);
            }
        }
    }
}
