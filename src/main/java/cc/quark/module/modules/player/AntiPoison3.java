package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AntiPoison3 — automatically uses a milk bucket when the player is poisoned,
 * withered, or has other harmful effects, curing them instantly.
 */
public class AntiPoison3 extends Module {

    private final BoolSetting antiPoison = register(new BoolSetting(
            "Poison", "Use milk when poisoned", true));
    private final BoolSetting antiWither = register(new BoolSetting(
            "Wither", "Use milk when withered", true));
    private final BoolSetting antiWeakness = register(new BoolSetting(
            "Weakness", "Use milk when weakened", false));
    private final BoolSetting antiSlowness = register(new BoolSetting(
            "Slowness", "Use milk when slowed", false));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between milk uses", 1500, 500, 10000));
    private final IntSetting healthThreshold = register(new IntSetting(
            "Min Health", "Only cure if health is below this value (0 = always)", 0, 0, 20));

    private final TimerUtil timer = new TimerUtil();

    public AntiPoison3() {
        super("AntiPoison3", "Automatically uses milk bucket when poisoned or withered", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    private boolean hasHarmfulEffect() {
        var effects = mc.player.getStatusEffects();
        for (var entry : effects) {
            var effect = entry.getEffectType().value();
            if (antiPoison.isEnabled()   && effect == StatusEffects.POISON.value())   return true;
            if (antiWither.isEnabled()   && effect == StatusEffects.WITHER.value())    return true;
            if (antiWeakness.isEnabled() && effect == StatusEffects.WEAKNESS.value())  return true;
            if (antiSlowness.isEnabled() && effect == StatusEffects.SLOWNESS.value())  return true;
        }
        return false;
    }

    private int findMilkSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.MILK_BUCKET)) return i;
        }
        return -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(delay.get())) return;
        if (!hasHarmfulEffect()) return;

        float health = mc.player.getHealth();
        int minHealth = healthThreshold.get();
        if (minHealth > 0 && health > minHealth) return;

        int milkSlot = findMilkSlot();
        if (milkSlot == -1) return;

        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = milkSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = saved;

        timer.reset();
    }
}
