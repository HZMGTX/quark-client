package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class PotionAura extends Module {

    private final ModeSetting type = register(new ModeSetting("Type", "Potion type to throw", "Harming", "Harming", "Poison", "Slowness"));
    private final IntSetting range = register(new IntSetting("Range", "Range to throw potions", 4, 1, 8));
    private final TimerUtil timer = new TimerUtil();

    public PotionAura() {
        super("PotionAura", "Auto-throws splash potions at the nearest enemy", Category.COMBAT);
    }

    private boolean matchesType(ItemStack stack) {
        if (!(stack.getItem() instanceof SplashPotionItem)) return false;
        var effects = PotionUtil.getPotionEffects(stack);
        for (var effect : effects) {
            String t = type.get();
            if (t.equals("Harming") && (effect.getEffectType() == StatusEffects.INSTANT_DAMAGE)) return true;
            if (t.equals("Poison") && (effect.getEffectType() == StatusEffects.POISON)) return true;
            if (t.equals("Slowness") && (effect.getEffectType() == StatusEffects.SLOWNESS)) return true;
        }
        return false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(1000)) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player || e.isDead() || EntityUtil.isFriend(e));
        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        if (targets.isEmpty()) return;

        int potionSlot = -1;
        int prevSlot = mc.player.getInventory().selectedSlot;
        for (int i = 0; i < 9; i++) {
            if (matchesType(mc.player.getInventory().getStack(i))) {
                potionSlot = i;
                break;
            }
        }
        if (potionSlot == -1) return;

        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(potionSlot));
        mc.player.getInventory().selectedSlot = potionSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        mc.player.getInventory().selectedSlot = prevSlot;
        timer.reset();
    }
}
