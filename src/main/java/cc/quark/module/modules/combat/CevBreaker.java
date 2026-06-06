package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * CevBreaker - Breaks crystal/obsidian box traps automatically.
 * Detects when the player is trapped in a CEV (crystal/obsidian/void) trap,
 * prioritises breaking end crystals first, then the surrounding obsidian cage.
 */
public class CevBreaker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to look for trap components (blocks)", 4.0, 2.0, 6.0));

    private final BoolSetting autoTool = register(new BoolSetting(
            "Auto Tool", "Switch to best pickaxe for obsidian mining", true));

    private final BoolSetting breakCrystals = register(new BoolSetting(
            "Break Crystals", "Break nearby end crystals first before obsidian", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between break actions", 100, 0, 500));

    private final TimerUtil timer = new TimerUtil();

    public CevBreaker() {
        super("CevBreaker", "Breaks crystal/obsidian box traps automatically", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Step 1: Break nearby end crystals if enabled
        if (breakCrystals.isEnabled()) {
            EndCrystalEntity crystal = findNearestCrystal();
            if (crystal != null) {
                attackCrystal(crystal);
                timer.reset();
                return;
            }
        }

        // Step 2: Break obsidian blocks that form the cage around the player
        BlockPos obsidianPos = findCageObsidian();
        if (obsidianPos == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoTool.isEnabled()) {
            int pickSlot = findPickaxeSlot();
            if (pickSlot != -1) mc.player.getInventory().selectedSlot = pickSlot;
        }

        // Determine the face to break from (face closest to player)
        Direction face = getFaceTowards(obsidianPos);
        mc.interactionManager.attackBlock(obsidianPos, face);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();

        if (autoTool.isEnabled()) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    /**
     * Find the nearest end crystal within range.
     */
    private EndCrystalEntity findNearestCrystal() {
        EndCrystalEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            double dist = mc.player.distanceTo(crystal);
            if (dist > range.get()) continue;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = crystal;
            }
        }
        return nearest;
    }

    /**
     * Find obsidian blocks that form a cage around the player (adjacent or near feet/head).
     * Returns the block with lowest hardness that is reachable and part of a trap enclosure.
     */
    private BlockPos findCageObsidian() {
        BlockPos playerPos = mc.player.getBlockPos();
        List<BlockPos> candidates = new ArrayList<>();

        // Check blocks in a small radius around the player
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    double dist = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
                    if (dist > range.get()) continue;

                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN) {
                        candidates.add(pos);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return null;

        // Pick the closest reachable obsidian block
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : candidates) {
            double dist = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }
        return best;
    }

    private void attackCrystal(EndCrystalEntity crystal) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(
                PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private Direction getFaceTowards(BlockPos pos) {
        Vec3d playerEye = mc.player.getEyePos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        Vec3d diff = playerEye.subtract(blockCenter);

        // Find the face whose normal is most aligned with the direction to the player
        double maxDot = Double.NEGATIVE_INFINITY;
        Direction best = Direction.UP;
        for (Direction dir : Direction.values()) {
            Vec3d normal = new Vec3d(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
            double dot = normal.dotProduct(diff.normalize());
            if (dot > maxDot) {
                maxDot = dot;
                best = dir;
            }
        }
        return best;
    }

    private int findPickaxeSlot() {
        float bestSpeed = -1f;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof PickaxeItem pick)) continue;
            float speed = pick.getMaterial().value().speed();
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
