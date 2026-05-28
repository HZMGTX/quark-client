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
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class RodAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Detection range in blocks", 5.0, 3.0, 8.0));
    private final IntSetting delayMs = register(new IntSetting("Delay ms", "Milliseconds between rod interactions", 400, 100, 1000));

    private final TimerUtil timer = new TimerUtil();
    private boolean castOut = false;
    private int rodSlot = -1;
    private int prevSlot = -1;

    public RodAura() {
        super("RodAura", "Auto-uses fishing rod to knock back nearby enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        castOut = false;
        rodSlot = -1;
        prevSlot = -1;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (prevSlot >= 0 && mc.player.getInventory().selectedSlot != prevSlot) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        rodSlot = findRodInHotbar();
        if (rodSlot == -1) return;

        if (mc.player.getInventory().selectedSlot != rodSlot) {
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = rodSlot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        castOut = !castOut;

        if (prevSlot >= 0 && !castOut) {
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
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get() || dist >= bestDist) continue;
            bestDist = dist;
            best = living;
        }
        return best;
    }

    private int findRodInHotbar() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.FISHING_ROD)) return i;
        }
        return -1;
    }
}
