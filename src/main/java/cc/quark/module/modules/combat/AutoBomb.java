package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBomb extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect enemies for bombing", 3.5, 1.0, 6.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between bomb placements", 500, 100, 3000));

    private final BoolSetting safeDistance = register(new BoolSetting(
            "Safe Distance", "Only bomb if player is far enough from TNT", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBomb() {
        super("AutoBomb", "Places and detonates TNT near enemies", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find nearest enemy
        Entity nearestEnemy = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < minDist) {
                minDist = dist;
                nearestEnemy = entity;
            }
        }

        if (nearestEnemy == null) return;

        // Safety check: don't place if we're too close
        if (safeDistance.isEnabled() && minDist < 2.5) return;

        // Find TNT in hotbar
        int tntSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TNT) {
                tntSlot = i;
                break;
            }
        }

        if (tntSlot < 0) return;

        // Place TNT between player and enemy
        Vec3d dir = nearestEnemy.getPos().subtract(mc.player.getPos()).normalize();
        BlockPos placePos = BlockPos.ofFloored(mc.player.getPos().add(dir.multiply(1.5)));

        // Ensure ground below is solid
        BlockPos below = placePos.down();
        if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) return;
        if (!mc.world.getBlockState(placePos).isAir()) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = tntSlot;

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(placePos), Direction.UP, below, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        mc.player.getInventory().selectedSlot = prev;
        timer.reset();
    }
}
