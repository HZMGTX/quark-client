package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

/**
 * AntiBlind2 - Removes vision-impairing potion effects every tick.
 * Extended edition with mining-fatigue and poison removal options.
 */
public class AntiBlind2 extends Module {

    private final BoolSetting blindness    = register(new BoolSetting("Blindness",     "Remove blindness effect",           true));
    private final BoolSetting darkness     = register(new BoolSetting("Darkness",      "Remove darkness effect",            true));
    private final BoolSetting nausea       = register(new BoolSetting("Nausea",        "Remove nausea effect",              true));
    private final BoolSetting levitation   = register(new BoolSetting("Levitation",    "Remove levitation effect",          false));
    private final BoolSetting slowness     = register(new BoolSetting("Slowness",      "Remove slowness effect",            false));
    private final BoolSetting weakness     = register(new BoolSetting("Weakness",      "Remove weakness effect",            false));
    private final BoolSetting miningFatigue = register(new BoolSetting("Mining Fatigue","Remove mining fatigue effect",     false));
    private final BoolSetting poison       = register(new BoolSetting("Poison",        "Remove poison effect",              false));
    private final BoolSetting wither       = register(new BoolSetting("Wither",        "Remove wither effect",              false));
    private final BoolSetting hunger       = register(new BoolSetting("Hunger",        "Remove hunger effect",              false));

    public AntiBlind2() {
        super("AntiBlind2", "Removes vision-impairing and debilitating potion effects", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (blindness.isEnabled())     mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (darkness.isEnabled())      mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        if (nausea.isEnabled())        mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        if (levitation.isEnabled())    mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        if (slowness.isEnabled())      mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        if (weakness.isEnabled())      mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
        if (miningFatigue.isEnabled()) mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        if (poison.isEnabled())        mc.player.removeStatusEffect(StatusEffects.POISON);
        if (wither.isEnabled())        mc.player.removeStatusEffect(StatusEffects.WITHER);
        if (hunger.isEnabled())        mc.player.removeStatusEffect(StatusEffects.HUNGER);
    }
}
