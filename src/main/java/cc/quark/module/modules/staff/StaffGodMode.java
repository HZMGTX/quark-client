package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class StaffGodMode extends Module {
    private final BoolSetting resistance = register(new BoolSetting("Resistance", "Apply resistance V", true));
    private final BoolSetting absorption = register(new BoolSetting("Absorption", "Apply absorption X", true));
    private final BoolSetting regen = register(new BoolSetting("Regen", "Apply regeneration II", true));

    public StaffGodMode() {
        super("Staff God", "Apply protection effects for staff inspection", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (resistance.isEnabled() && (mc.player.getStatusEffect(StatusEffects.RESISTANCE) == null ||
                mc.player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() < 40)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 4, false, false));
        }
        if (absorption.isEnabled() && (mc.player.getStatusEffect(StatusEffects.ABSORPTION) == null ||
                mc.player.getStatusEffect(StatusEffects.ABSORPTION).getDuration() < 40)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 9, false, false));
        }
        if (regen.isEnabled() && (mc.player.getStatusEffect(StatusEffects.REGENERATION) == null ||
                mc.player.getStatusEffect(StatusEffects.REGENERATION).getDuration() < 40)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.RESISTANCE);
        mc.player.removeStatusEffect(StatusEffects.ABSORPTION);
        mc.player.removeStatusEffect(StatusEffects.REGENERATION);
    }
}
