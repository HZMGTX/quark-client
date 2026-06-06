package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * TrapFight - combines surround placement with end-crystal attacks for
 * efficient trap kills.  When a target is nearby it:
 *   1. Places obsidian around the target (N/S/E/W) to lock them in.
 *   2. Simultaneously attacks with crystal aura logic via
 *      {@code interactionManager.attackEntity} on crystal entities.
 *
 * Requires obsidian in the hotbar for trap placement.
 */
public class TrapFight extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Target detection range", 5.0, 2.0, 8.0));

    private final BoolSetting placeObsidian = register(new BoolSetting(
            "Place Obsidian", "Surround the target with obsidian", true));

    private final BoolSetting attackCrystals = register(new BoolSetting(
            "Attack Crystals", "Attack end crystals near the trapped target", true));

    private final BoolSetting autoSwap = register(new BoolSetting(
            "Auto Swap", "Switch to obsidian automatically", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between placement actions", 50, 0, 500));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public TrapFight() {
        super("TrapFight", "Combines obsidian trap with crystal attacks", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        PlayerEntity target = findTarget();
        if (target == null) return;

        // --- Crystal attack phase ---
        if (attackCrystals.isEnabled()) {
            for (net.minecraft.entity.Entity e : mc.world.getEntities()) {
                if (!(e instanceof net.minecraft.entity.decoration.EndCrystalEntity crystal)) continue;
                double crystalDist = mc.player.distanceTo(crystal);
                if (crystalDist > range.get() + 2.0) continue;
                double crystalToTarget = crystal.distanceTo(target);
                if (crystalToTarget > 4.0) continue; // only crystals that will damage target
                mc.interactionManager.attackEntity(mc.player, crystal);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }

        // --- Obsidian trap placement phase ---
        if (!placeObsidian.isEnabled()) { timer.reset(); return; }

        int obsSlot = findObsidianSlot();
        if (obsSlot == -1) { timer.reset(); return; }

        int curSlot = mc.player.getInventory().selectedSlot;
        if (curSlot != obsSlot) {
            if (!autoSwap.isEnabled()) { timer.reset(); return; }
            prevSlot = curSlot;
            mc.player.getInventory().selectedSlot = obsSlot;
        }

        // Try to place on each cardinal side of the target
        BlockPos targetPos = target.getBlockPos();
        Direction[] dirs = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
        for (Direction dir : dirs) {
            BlockPos placePos = targetPos.offset(dir);
            // Only place at foot level
            if (!mc.world.getBlockState(placePos).isAir()) continue;
            if (!mc.world.getBlockState(placePos.down()).isAir()
                    && mc.world.getBlockState(placePos.down()).isSolid()) {
                // Place on top of solid floor
                Vec3d hitVec = Vec3d.ofCenter(placePos.down()).add(0, 0.5, 0);
                BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, placePos.down(), false);
                double distToPlace = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(placePos));
                if (distToPlace <= range.get() + 1.5) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        }

        restoreSlot();
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

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi
                    && bi.getBlock() == net.minecraft.block.Blocks.OBSIDIAN) return i;
        }
        return -1;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
