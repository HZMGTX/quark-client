package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ThrowPot extends Module {
    private final IntSetting range = register(new IntSetting("Range", "Range to throw at enemies", 4, 1, 8));
    private final BoolSetting harmPot = register(new BoolSetting("Harm", "Throw harming potions", true));
    private final BoolSetting poisonPot = register(new BoolSetting("Poison", "Throw poison potions", false));
    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil throwTimer = new TimerUtil();

    public ThrowPot() { super("ThrowPot", "Auto-throws splash potions at enemies", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        if (!throwTimer.hasReached(800)) return;

        PlayerEntity nearest = null;
        double closest = range.get();
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof PlayerEntity pe) || pe == mc.player) continue;
            double d = mc.player.distanceTo(pe);
            if (d < closest) { closest = d; nearest = pe; }
        }
        if (nearest == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || stack.getItem() != Items.SPLASH_POTION) continue;
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) continue;
            boolean shouldThrow = false;
            for (var eff : contents.getEffects()) {
                var et = eff.getEffectType().value();
                if (harmPot.isEnabled() && et == StatusEffects.INSTANT_DAMAGE.value()) shouldThrow = true;
                if (poisonPot.isEnabled() && et == StatusEffects.POISON.value()) shouldThrow = true;
            }
            if (shouldThrow) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
                throwTimer.reset();
                return;
            }
        }
    }
}
