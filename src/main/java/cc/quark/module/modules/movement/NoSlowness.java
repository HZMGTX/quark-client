package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;

/**
 * NoSlowness - removes Slowness and Mining Fatigue status effects each tick.
 * Distinct from NoSlowdown which cancels terrain/item-use movement penalties.
 * Per-effect BoolSettings allow individual toggling.
 */
public class NoSlowness extends Module {

    private final BoolSetting removeSlowness = register(new BoolSetting(
            "Slowness", "Remove the Slowness status effect", true));
    private final BoolSetting removeMiningFatigue = register(new BoolSetting(
            "MiningFatigue", "Remove the Mining Fatigue status effect", true));
    private final BoolSetting removeWeakness = register(new BoolSetting(
            "Weakness", "Remove the Weakness status effect", false));
    private final BoolSetting removeNausea = register(new BoolSetting(
            "Nausea", "Remove the Nausea status effect", false));
    private final BoolSetting removeBadLuck = register(new BoolSetting(
            "BadLuck", "Remove the Unluck status effect", false));

    public NoSlowness() {
        super("NoSlowness", "Removes Slowness and Mining Fatigue status effects", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (removeSlowness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        }
        if (removeMiningFatigue.isEnabled() && mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
        if (removeWeakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
        }
        if (removeNausea.isEnabled() && mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
        if (removeBadLuck.isEnabled() && mc.player.hasStatusEffect(StatusEffects.UNLUCK)) {
            mc.player.removeStatusEffect(StatusEffects.UNLUCK);
        }
    }
}
