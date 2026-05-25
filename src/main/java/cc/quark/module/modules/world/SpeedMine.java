package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SpeedMine extends Module {

    private final IntSetting level = register(new IntSetting(
            "Haste Level", "Haste amplifier applied (1 = Haste I, 2 = Haste II, etc.)", 2, 1, 10));
    private final BoolSetting removeFatigue = register(new BoolSetting(
            "Remove Fatigue", "Remove mining fatigue effect", true));

    public SpeedMine() {
        super("SpeedMine", "Applies Haste and removes Mining Fatigue for faster mining", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (removeFatigue.isEnabled() && mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }

        int amp = level.get() - 1;
        StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.HASTE);
        if (current == null || current.getAmplifier() < amp) {
            mc.player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.HASTE, 100, amp, false, false));
        }
    }
}
