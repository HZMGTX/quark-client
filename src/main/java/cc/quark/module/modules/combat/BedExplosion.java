package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * BedExplosion - places and immediately detonates beds in the Nether or End
 * to deal explosion damage to nearby enemies.  Respects anti-suicide and
 * dimension checks so it never fires in the Overworld.
 */
public class BedExplosion extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Target search range", 5.0, 2.0, 8.0));

    private final BoolSetting antiSuicide = register(new BoolSetting(
            "Anti Suicide", "Skip if self damage would kill you", true));

    private final BoolSetting autoSwap = register(new BoolSetting(
            "Auto Swap", "Auto-switch to a bed in the hotbar", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between placements", 100, 0, 1000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    // State machine: 0 = find & place, 1 = detonate placed bed
    private BlockPos placedBed = null;

    public BedExplosion() {
        super("BedExplosion", "Places and detonates beds in Nether/End for damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        placedBed = null;
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
        placedBed = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only activate in Nether or End
        World.Type dimType = mc.world.getDimension().ultrawarm() ? World.Type.OTHER : World.Type.OTHER;
        // Check dimension by registry key name
        String dimKey = mc.world.getRegistryKey().getValue().getPath();
        boolean validDim = dimKey.contains("nether") || dimKey.contains("end");
        if (!validDim) return;

        if (!timer.hasReached(delay.get())) return;

        // --- Phase 2: detonate the placed bed ---
        if (placedBed != null) {
            if (mc.world.getBlockState(placedBed).getBlock() instanceof BedBlock) {
                Vec3d hitVec = Vec3d.ofCenter(placedBed);
                BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, placedBed, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            placedBed = null;
            restoreSlot();
            timer.reset();
            return;
        }

        // --- Phase 1: find target, find bed, place bed ---
        PlayerEntity target = findTarget();
        if (target == null) return;

        int bedSlot = findBedSlot();
        if (bedSlot == -1) return;

        int curSlot = mc.player.getInventory().selectedSlot;
        if (curSlot != bedSlot) {
            if (!autoSwap.isEnabled()) return;
            prevSlot = curSlot;
            mc.player.getInventory().selectedSlot = bedSlot;
        }

        BlockPos placePos = findPlacementPos(target);
        if (placePos == null) {
            restoreSlot();
            return;
        }

        // Anti-suicide: rough health check (bed explosion ~40 HP unprotected)
        if (antiSuicide.isEnabled()) {
            double selfDist = mc.player.getPos().distanceTo(Vec3d.ofCenter(placePos));
            if (selfDist < 3.0 && mc.player.getHealth() < 12f) {
                restoreSlot();
                return;
            }
        }

        Vec3d hitVec = Vec3d.ofCenter(placePos.down()).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, placePos.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        placedBed = placePos;
        timer.reset();
    }

    private PlayerEntity findTarget() {
        PlayerEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (net.minecraft.entity.Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p) || p == mc.player) continue;
            if (p.isRemoved() || p.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(p);
            if (d <= range.get() && d < best) { best = d; nearest = p; }
        }
        return nearest;
    }

    private int findBedSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof BedBlock) return i;
        }
        return -1;
    }

    private BlockPos findPlacementPos(PlayerEntity target) {
        BlockPos tp = target.getBlockPos();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            BlockPos candidate = tp.offset(dir);
            if (mc.world.getBlockState(candidate).isAir()
                    && mc.world.getBlockState(candidate.up()).isAir()) {
                double d = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(candidate));
                if (d <= range.get() + 1.5) return candidate;
            }
        }
        return mc.world.getBlockState(tp).isAir() ? tp : null;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
