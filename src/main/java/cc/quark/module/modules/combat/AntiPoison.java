package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiPoison extends Module {

    private final BoolSetting poison    = register(new BoolSetting("Poison",    "Remove poison effect",      true));
    private final BoolSetting wither    = register(new BoolSetting("Wither",    "Remove wither effect",      true));
    private final BoolSetting fire      = register(new BoolSetting("Fire",      "Extinguish fire",           true));
    private final BoolSetting hunger    = register(new BoolSetting("Hunger",    "Remove hunger effect",      false));
    private final BoolSetting badOmen   = register(new BoolSetting("Bad Omen",  "Remove Bad Omen",           false));

    public AntiPoison() {
        super("AntiPoison", "Removes poison, wither, fire and other damaging effects", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (poison.isEnabled())  mc.player.removeStatusEffect(StatusEffects.POISON);
        if (wither.isEnabled())  mc.player.removeStatusEffect(StatusEffects.WITHER);
        if (fire.isEnabled() && mc.player.isOnFire()) mc.player.extinguish();
        if (hunger.isEnabled())  mc.player.removeStatusEffect(StatusEffects.HUNGER);
        if (badOmen.isEnabled()) mc.player.removeStatusEffect(StatusEffects.BAD_OMEN);
    }
}
