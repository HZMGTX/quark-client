package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

public class AntiDebuff extends Module {

    private final BoolSetting slowness = register(new BoolSetting("Slowness", "Remove slowness effect", true));
    private final BoolSetting miningFatigue = register(new BoolSetting("Mining Fatigue", "Remove mining fatigue effect", true));
    private final BoolSetting nausea = register(new BoolSetting("Nausea", "Remove nausea effect", true));
    private final BoolSetting blindness = register(new BoolSetting("Blindness", "Remove blindness effect", true));
    private final BoolSetting hunger = register(new BoolSetting("Hunger", "Remove hunger effect", true));
    private final BoolSetting weakness = register(new BoolSetting("Weakness", "Remove weakness effect", true));
    private final BoolSetting poison = register(new BoolSetting("Poison", "Remove poison effect", true));
    private final BoolSetting wither = register(new BoolSetting("Wither", "Remove wither effect", true));
    private final BoolSetting levitation = register(new BoolSetting("Levitation", "Remove levitation effect", false));
    private final BoolSetting badOmen = register(new BoolSetting("Bad Omen", "Remove bad omen effect", false));

    public AntiDebuff() {
        super("AntiDebuff", "Removes negative status effects each tick", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (slowness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        if (miningFatigue.isEnabled() && mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        if (nausea.isEnabled() && mc.player.hasStatusEffect(StatusEffects.NAUSEA)) mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        if (blindness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (hunger.isEnabled() && mc.player.hasStatusEffect(StatusEffects.HUNGER)) mc.player.removeStatusEffect(StatusEffects.HUNGER);
        if (weakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
        if (poison.isEnabled() && mc.player.hasStatusEffect(StatusEffects.POISON)) mc.player.removeStatusEffect(StatusEffects.POISON);
        if (wither.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WITHER)) mc.player.removeStatusEffect(StatusEffects.WITHER);
        if (levitation.isEnabled() && mc.player.hasStatusEffect(StatusEffects.LEVITATION)) mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        if (badOmen.isEnabled() && mc.player.hasStatusEffect(StatusEffects.BAD_OMEN)) mc.player.removeStatusEffect(StatusEffects.BAD_OMEN);
    }
}
