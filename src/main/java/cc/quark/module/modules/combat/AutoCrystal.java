package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.RotationManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.DamageUtil;
import cc.quark.util.EntityUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.*;

public class AutoCrystal extends Module {

    private static final float CRYSTAL_RADIUS = 6.0f;

    private final DoubleSetting placeRange = register(new DoubleSetting(
            "Place Range", "Range to place crystals (blocks)", 4.0, 2.0, 6.0));

    private final DoubleSetting breakRange = register(new DoubleSetting(
            "Break Range", "Range to break crystals (blocks)", 4.0, 2.0, 6.0));

    private final DoubleSetting minDamage = register(new DoubleSetting(
            "Min Damage", "Minimum damage to target before placing", 6.0, 4.0, 20.0));

    private final DoubleSetting maxSelfDamage = register(new DoubleSetting(
            "Max Self Damage", "Maximum self-damage allowed when placing", 6.0, 0.0, 10.0));

    private final IntSetting placeDelay = register(new IntSetting(
            "Place Delay", "Ticks between crystal placements", 1, 0, 10));

    private final IntSetting breakDelay = register(new IntSetting(
            "Break Delay", "Ticks between crystal breaks", 0, 0, 10));

    private final BoolSetting rotate = register(new BoolSetting(
            "Rotate", "Silently rotate toward crystal positions using RotationManager", true));

    private final BoolSetting antiSuicide = register(new BoolSetting(
            "Anti Suicide", "Skip placement if self-damage exceeds 85% of current health", true));

    private final BoolSetting smartPlace = register(new BoolSetting(
            "Smart Place", "Find the best position that maximizes target damage while minimizing self damage", true));

    private final BoolSetting renderPlace = register(new BoolSetting(
            "Render Place", "Highlight positions where crystals will be placed", true));

    private final BoolSetting renderBreak = register(new BoolSetting(
            "Render Break", "Highlight crystals about to be broken", true));

    private int ticksSincePlace = 0;
    private int ticksSinceBreak = 0;
    private BlockPos lastPlacePos = null;
    private PlayerEntity cachedTarget = null;

    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks end crystals for damage",
                Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        if (cachedTarget != null && !cachedTarget.isRemoved() && cachedTarget.getHealth() > 0) {
            return "[" + cachedTarget.getGameProfile().getName() + "]";
        }
        return "";
    }

    @Override
    public void onEnable() {
        ticksSincePlace = 0;
        ticksSinceBreak = 0;
        lastPlacePos = null;
        cachedTarget = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ticksSincePlace++;
        ticksSinceBreak++;

        // Update target reference
        cachedTarget = findBestDpsTarget();

        // Break existing crystals first (separate delay)
        if (ticksSinceBreak > breakDelay.get()) {
            EndCrystalEntity crystalToBreak = findBestCrystalToBreak();
            if (crystalToBreak != null) {
                if (rotate.isEnabled()) {
                    Vec3d crystalPos = crystalToBreak.getEyePos();
                    float yaw = (float) Math.toDegrees(Math.atan2(
                            -(crystalPos.x - mc.player.getEyePos().x),
                            crystalPos.z - mc.player.getEyePos().z));
                    float pitch = (float) Math.toDegrees(Math.atan2(
                            -(crystalPos.y - mc.player.getEyePos().y),
                            Math.sqrt(Math.pow(crystalPos.x - mc.player.getEyePos().x, 2) +
                                    Math.pow(crystalPos.z - mc.player.getEyePos().z, 2))));
                    RotationManager.INSTANCE.requestRotation(yaw, MathHelper.clamp(pitch, -90f, 90f), 5, true);
                }
                attackCrystal(crystalToBreak);
                ticksSinceBreak = 0;
                return;
            }
        }

        // Place new crystals
        if (ticksSincePlace <= placeDelay.get()) return;

        PlayerEntity target = cachedTarget;
        if (target == null) return;

        BlockPos placePos = smartPlace.isEnabled()
                ? findSmartPlacement(target)
                : findBestPlacement(target);
        if (placePos == null) return;

        // Anti Suicide check using DamageUtil
        if (antiSuicide.isEnabled()) {
            Vec3d crystalCenter = Vec3d.ofCenter(placePos.up());
            float selfDmg = DamageUtil.getSelfDamage(crystalCenter);
            float playerHealth = mc.player.getHealth();
            if (selfDmg > playerHealth * 0.85f) return;
        }

        // Switch to crystal slot in hotbar
        int crystalSlot = findCrystalSlot();
        if (crystalSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = crystalSlot;

        // Rotate toward placement position if enabled
        if (rotate.isEnabled()) {
            Vec3d crystalCenter = Vec3d.ofCenter(placePos.up());
            float yaw = (float) Math.toDegrees(Math.atan2(
                    -(crystalCenter.x - mc.player.getEyePos().x),
                    crystalCenter.z - mc.player.getEyePos().z));
            float pitch = (float) Math.toDegrees(Math.atan2(
                    -(crystalCenter.y - mc.player.getEyePos().y),
                    Math.sqrt(Math.pow(crystalCenter.x - mc.player.getEyePos().x, 2) +
                            Math.pow(crystalCenter.z - mc.player.getEyePos().z, 2))));
            RotationManager.INSTANCE.requestRotation(yaw, MathHelper.clamp(pitch, -90f, 90f), 5, true);
        }

        placeCrystal(placePos);
        lastPlacePos = placePos;
        ticksSincePlace = 0;

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    /**
     * Smart placement: tries positions around the target (4 cardinal blocks) and picks
     * the one with the best damage-to-self-damage ratio.
     */
    private BlockPos findSmartPlacement(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
        int[] offsets = {0, 1, -1, 2, -2};
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos bestPos = null;

        // Try positions around the target
        int[][] candidates = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] offset : candidates) {
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos pos = targetPos.add(offset[0], dy, offset[1]);

                var base = mc.world.getBlockState(pos).getBlock();
                if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK) continue;

                BlockPos above = pos.up();
                if (!mc.world.getBlockState(above).isAir()) continue;

                BlockPos aboveAbove = above.up();
                if (!mc.world.getBlockState(aboveAbove).isAir()) continue;

                Vec3d crystalCenter = Vec3d.ofCenter(above);
                if (mc.player.getEyePos().distanceTo(crystalCenter) > placeRange.get()) continue;

                double dmgToTarget = calcExplosionDamage(target, crystalCenter);
                double dmgToSelf = calcExplosionDamage(mc.player, crystalCenter);

                if (dmgToTarget < minDamage.get()) continue;
                if (dmgToSelf > maxSelfDamage.get()) continue;

                // Smart score: maximize target damage / (self damage + 1) ratio
                double score = dmgToTarget / (dmgToSelf + 1.0);
                if (score > bestScore) {
                    bestScore = score;
                    bestPos = pos;
                }
            }
        }

        // Fall back to normal placement if smart placement found nothing
        return bestPos != null ? bestPos : findBestPlacement(target);
    }

    private BlockPos findBestPlacement(PlayerEntity target) {
        BlockPos playerPos = mc.player.getBlockPos();
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos bestPos = null;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);

                    var base = mc.world.getBlockState(pos).getBlock();
                    if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK) continue;

                    BlockPos above = pos.up();
                    if (!mc.world.getBlockState(above).isAir()) continue;

                    BlockPos aboveAbove = above.up();
                    if (!mc.world.getBlockState(aboveAbove).isAir()) continue;

                    Vec3d crystalCenter = Vec3d.ofCenter(above);
                    if (mc.player.getEyePos().distanceTo(crystalCenter) > placeRange.get()) continue;

                    double dmgToTarget = calcExplosionDamage(target, crystalCenter);
                    double dmgToSelf = calcExplosionDamage(mc.player, crystalCenter);

                    if (dmgToTarget < minDamage.get()) continue;
                    if (dmgToSelf > maxSelfDamage.get()) continue;

                    double score = dmgToTarget - dmgToSelf * 0.5;
                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private void placeCrystal(BlockPos basePos) {
        BlockPos above = basePos.up();
        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(above),
                Direction.UP,
                basePos,
                false);

        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            mc.getNetworkHandler().sendPacket(
                    new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    private EndCrystalEntity findBestCrystalToBreak() {
        EndCrystalEntity best = null;
        double bestDmg = Double.NEGATIVE_INFINITY;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (EntityUtil.distanceTo(crystal) > breakRange.get()) continue;

            PlayerEntity nearestTarget = findBestDpsTarget();
            if (nearestTarget == null) continue;

            Vec3d crystalPos = crystal.getPos().add(0, 1, 0);
            double dmg = calcExplosionDamage(nearestTarget, crystalPos);

            if (dmg < minDamage.get()) continue;
            double selfDmg = calcExplosionDamage(mc.player, crystalPos);
            if (selfDmg > maxSelfDamage.get()) continue;

            if (dmg > bestDmg) {
                bestDmg = dmg;
                best = crystal;
            }
        }
        return best;
    }

    private void attackCrystal(EndCrystalEntity crystal) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(
                PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(
                new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private double calcExplosionDamage(net.minecraft.entity.LivingEntity entity, Vec3d explosionPos) {
        double dist = entity.getPos().add(0, entity.getHeight() / 2.0, 0)
                .distanceTo(explosionPos);
        if (dist > CRYSTAL_RADIUS) return 0;

        double exposure = 1.0 - (dist / CRYSTAL_RADIUS);
        double impact = exposure * (0.7 + CRYSTAL_RADIUS * 0.3);
        return (impact * impact + impact) * 7.0 * CRYSTAL_RADIUS + 1.0;
    }

    private PlayerEntity findBestDpsTarget() {
        if (mc.world == null) return null;
        PlayerEntity bestTarget = null;
        double bestDps = Double.NEGATIVE_INFINITY;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isRemoved() || p.getHealth() <= 0) continue;

            double d = EntityUtil.distanceTo(p);
            if (d > placeRange.get() + 4) continue;

            // Score based on proximity and health (closer + lower health = higher priority)
            double dps = (1.0 / Math.max(d, 0.1)) * (1.0 / Math.max(p.getHealth(), 0.1));
            if (dps > bestDps) {
                bestDps = dps;
                bestTarget = p;
            }
        }
        return bestTarget;
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                return i;
            }
        }
        return -1;
    }
}
