package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class GodMode extends Module {

    private final BoolSetting cancelDamage = register(new BoolSetting(
            "Cancel Damage", "Cancel all incoming damage events", true));
    private final BoolSetting setMaxHealth = register(new BoolSetting(
            "Set Max Health", "Set health to max each tick (client-side)", true));
    private final BoolSetting absorption = register(new BoolSetting(
            "Absorption", "Apply absorption hearts effect each tick", true));
    private final IntSetting resistLevel = register(new IntSetting(
            "Resistance Level", "Resistance effect amplifier (0=Resist I)", 4, 0, 4));

    public GodMode() {
        super("GodMode", "Client-side invincibility using resistance, regeneration and damage cancellation", Category.PLAYER);
    }

    @Override
    public String getSuffix() {
        return "ACTIVE";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Apply resistance and regen each tick (duration 40 = 2s, refreshed every tick)
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 40, resistLevel.get(), false, false));
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 40, 1, false, false));

        if (setMaxHealth.isEnabled()) {
            mc.player.setHealth(mc.player.getMaxHealth());
        }
        if (absorption.isEnabled()) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.ABSORPTION, 40, 3, false, false));
        }
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (cancelDamage.isEnabled()) {
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.RESISTANCE);
        mc.player.removeStatusEffect(StatusEffects.REGENERATION);
        mc.player.removeStatusEffect(StatusEffects.ABSORPTION);
    }
}
