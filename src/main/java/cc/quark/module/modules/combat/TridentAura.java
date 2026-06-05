package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.RotationManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RotationUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class TridentAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 8.0, 1.0, 15.0));
    private final DoubleSetting delay = register(new DoubleSetting("Delay ms", "Milliseconds between trident throws", 800, 200, 3000));

    private final TimerUtil timer = new TimerUtil();
    private int tridentSlot = -1;
    private int prevSlot = -1;

    public TridentAura() {
        super("TridentAura", "Auto-throws trident at nearby enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        tridentSlot = -1;
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (mc.player.isUsingItem()) {
            mc.player.stopUsingItem();
        }
        if (prevSlot >= 0 && mc.player.getInventory().selectedSlot != prevSlot) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        if (!isInRainOrWater()) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        tridentSlot = findTridentInHotbar();
        if (tridentSlot == -1) return;

        if (mc.player.getInventory().selectedSlot != tridentSlot) {
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = tridentSlot;
        }

        Vec3d targetPos = target.getEyePos();
        float yaw = RotationUtil.getYaw(targetPos);
        float pitch = RotationUtil.getPitch(targetPos);
        RotationManager.INSTANCE.requestRotation(yaw, pitch, 10, true);

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (prevSlot >= 0) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }

        timer.reset();
    }

    private boolean isInRainOrWater() {
        if (mc.player == null || mc.world == null) return false;
        return mc.player.isTouchingWater() || mc.world.hasRain(mc.player.getBlockPos());
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

    private int findTridentInHotbar() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.TRIDENT)) return i;
        }
        return -1;
    }
}
