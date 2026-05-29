package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

/**
 * AntiPoison — removes Poison and Wither effects every tick.
 * Separate BoolSettings allow toggling each effect individually.
 */
public class AntiPoison extends Module {

    private final BoolSetting poison  = register(new BoolSetting("Poison",  "Remove Poison effect",         true));
    private final BoolSetting wither  = register(new BoolSetting("Wither",  "Remove Wither effect",         true));
    private final BoolSetting fire    = register(new BoolSetting("Fire",    "Extinguish fire on player",    false));
    private final BoolSetting hunger  = register(new BoolSetting("Hunger",  "Remove Hunger effect",         false));
    private final BoolSetting badOmen = register(new BoolSetting("Bad Omen","Remove Bad Omen effect",       false));
    private final BoolSetting slow    = register(new BoolSetting("Slowness","Remove Slowness effect",       false));

    public AntiPoison() {
        super("AntiPoison", "Removes poison, wither, and other debuff effects every tick", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (poison.isEnabled()  && mc.player.hasStatusEffect(StatusEffects.POISON))   mc.player.removeStatusEffect(StatusEffects.POISON);
        if (wither.isEnabled()  && mc.player.hasStatusEffect(StatusEffects.WITHER))   mc.player.removeStatusEffect(StatusEffects.WITHER);
        if (fire.isEnabled()    && mc.player.isOnFire())                               mc.player.extinguish();
        if (hunger.isEnabled()  && mc.player.hasStatusEffect(StatusEffects.HUNGER))   mc.player.removeStatusEffect(StatusEffects.HUNGER);
        if (badOmen.isEnabled() && mc.player.hasStatusEffect(StatusEffects.BAD_OMEN)) mc.player.removeStatusEffect(StatusEffects.BAD_OMEN);
        if (slow.isEnabled()    && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
    }
}
