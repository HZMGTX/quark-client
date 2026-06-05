package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.util.Hand;

public class PoisonAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to throw poison at targets", 8.0, 4.0, 15.0));
    private final IntSetting delayMs = register(new IntSetting("Delay ms", "Milliseconds between throws", 1500, 500, 3000));

    private final TimerUtil timer = new TimerUtil();
    private int potionSlot = -1;
    private int prevSlot = -1;

    public PoisonAura() {
        super("PoisonAura", "Auto-throws splash poison potions at nearby enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        potionSlot = -1;
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && prevSlot >= 0 && mc.player.getInventory().selectedSlot != prevSlot) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        potionSlot = findPoisonPotionInHotbar();
        if (potionSlot == -1) return;

        if (mc.player.getInventory().selectedSlot != potionSlot) {
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = potionSlot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (prevSlot >= 0) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }

        timer.reset();
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get() || dist >= bestDist) continue;
            bestDist = dist;
            best = living;
        }
        return best;
    }

    private int findPoisonPotionInHotbar() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SplashPotionItem) return i;
        }
        return -1;
    }
}
