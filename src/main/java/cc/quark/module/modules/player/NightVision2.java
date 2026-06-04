package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class NightVision2 extends Module {

    private final BoolSetting always = register(new BoolSetting(
            "Always On", "Keep night vision active at all times regardless of conditions", true));

    public NightVision2() {
        super("NightVision2", "Applies night vision effect client-side without potion", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        if (mc.options != null) {
            mc.options.getGamma().setValue(1.0);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (always.isEnabled()) {
            // Apply via gamma boost
            mc.options.getGamma().setValue(16.0);

            // Also apply Night Vision effect client-side
            StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (current == null || current.getDuration() < 210) {
                mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
            }
        } else {
            // Only apply when it's dark (light level < 7)
            int lightLevel = mc.world.getLightLevel(mc.player.getBlockPos());
            if (lightLevel < 7) {
                mc.options.getGamma().setValue(16.0);
                StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                if (current == null || current.getDuration() < 210) {
                    mc.player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
                }
            } else {
                mc.options.getGamma().setValue(1.0);
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
    }
}
