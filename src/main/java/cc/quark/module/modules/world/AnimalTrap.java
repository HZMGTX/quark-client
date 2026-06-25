package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AnimalTrap extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to look for animals and trap spots", 3.0, 1.0, 6.0));
    private final ModeSetting trap = register(new ModeSetting(
            "Trap", "Trap type to build around animals",
            "Pit", "Pit", "Fence"));

    private final TimerUtil timer = new TimerUtil();

    public AnimalTrap() {
        super("AnimalTrap", "Places fence or digs pits around nearby passive animals for farming", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;
        timer.reset();

        double rangeSq = range.get() * range.get();

        // Find nearest passable animal
        PassiveEntity target = null;
        double nearestDist = rangeSq;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PassiveEntity passive)) continue;
            if (passive instanceof VillagerEntity) continue;
            double dist = mc.player.squaredDistanceTo(passive);
            if (dist < nearestDist) {
                nearestDist = dist;
                target = passive;
            }
        }

        if (target == null) return;

        BlockPos animalPos = target.getBlockPos();

        if (trap.get().equals("Fence")) {
            placeFenceAround(animalPos);
        } else {
            // Pit: break blocks below the animal
            BlockPos below = animalPos.down();
            if (!mc.world.getBlockState(below).isAir()) {
                mc.interactionManager.attackBlock(below, Direction.UP);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private void placeFenceAround(BlockPos center) {
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;

        // Find oak fence in hotbar
        int fenceSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.OAK_FENCE)) {
                fenceSlot = i;
                break;
            }
        }
        if (fenceSlot == -1) return;

        // Place fence on 4 adjacent sides
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction dir : dirs) {
            BlockPos fencePos = center.offset(dir);
            if (!mc.world.getBlockState(fencePos).isAir()) continue;
            BlockPos supportPos = fencePos.down();
            if (!mc.world.getBlockState(supportPos).isSolidBlock(mc.world, supportPos)) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = fenceSlot;
            Vec3d hitVec = Vec3d.ofCenter(supportPos).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, supportPos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            return; // One fence per tick
        }
    }
}
