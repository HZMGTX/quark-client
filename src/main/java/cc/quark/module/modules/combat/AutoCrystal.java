package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
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

/**
 * AutoCrystal - places End Crystals on obsidian/bedrock at positions that
 * maximise damage to nearby players, then immediately attacks them to trigger
 * the explosion.
 *
 * <p>Damage calculation uses the vanilla explosion formula:
 * <pre>
 *   exposure = 1 - (dist / (2 * radius))
 *   damage   = (exposure^2 * 7 + exposure) * 6 * 0.85 / 2
 * </pre>
 * where {@code radius = 6.0} for an end crystal explosion.
 */
public class AutoCrystal extends Module {

    /** Vanilla end-crystal explosion radius (blocks). */
    private static final float CRYSTAL_RADIUS = 6.0f;

    // ---- Settings ----
    private final DoubleSetting placeRange = register(new DoubleSetting(
            "Place Range", "Range to place crystals (blocks)", 4.0, 2.0, 6.0));

    private final DoubleSetting breakRange = register(new DoubleSetting(
            "Break Range", "Range to break crystals (blocks)", 4.0, 2.0, 6.0));

    private final DoubleSetting minDamage = register(new DoubleSetting(
            "Min Damage", "Minimum damage to target before placing", 6.0, 4.0, 20.0));

    private final DoubleSetting maxSelfDamage = register(new DoubleSetting(
            "Max Self Damage", "Maximum self-damage allowed when placing", 6.0, 0.0, 10.0));

    private final BoolSetting renderPlace = register(new BoolSetting(
            "Render Place", "Highlight positions where crystals will be placed", true));

    private final BoolSetting renderBreak = register(new BoolSetting(
            "Render Break", "Highlight crystals about to be broken", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between place/break actions", 1, 0, 5));

    // ---- State ----
    private int ticksSinceLast = 0;
    /** Last position we placed a crystal at; used to immediately break it. */
    private BlockPos lastPlacePos = null;

    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks end crystals for damage",
                Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticksSinceLast = 0;
        lastPlacePos   = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ticksSinceLast++;
        if (ticksSinceLast < delay.get()) return;
        ticksSinceLast = 0;

        // ---- Break existing crystals first ----
        EndCrystalEntity crystalToBreak = findBestCrystalToBreak();
        if (crystalToBreak != null) {
            attackCrystal(crystalToBreak);
            return; // Break takes priority this tick
        }

        // ---- Find nearest target player ----
        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        // ---- Find best placement position ----
        BlockPos placePos = findBestPlacement(target);
        if (placePos == null) return;

        // ---- Check we're holding an end crystal ----
        if (!holdingCrystal()) {
            switchToCrystal();
            if (!holdingCrystal()) return;
        }

        placeCrystal(placePos);
        lastPlacePos = placePos;
    }

    // -------------------------------------------------------------------------
    // Crystal placement
    // -------------------------------------------------------------------------

    private BlockPos findBestPlacement(PlayerEntity target) {
        BlockPos playerPos = mc.player.getBlockPos();
        double bestScore   = Double.NEGATIVE_INFINITY;
        BlockPos bestPos   = null;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);

                    // Must be obsidian or bedrock beneath the crystal
                    var base = mc.world.getBlockState(pos).getBlock();
                    if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK) continue;

                    // The block on top must be air
                    BlockPos above = pos.up();
                    if (!mc.world.getBlockState(above).isAir()) continue;

                    // The block two above also must be air (crystal occupies 2 blocks)
                    BlockPos aboveAbove = above.up();
                    if (!mc.world.getBlockState(aboveAbove).isAir()) continue;

                    // Must be within place range
                    Vec3d crystalCenter = Vec3d.ofCenter(above);
                    if (mc.player.getEyePos().distanceTo(crystalCenter) > placeRange.get()) continue;

                    // Damage calculations
                    double dmgToTarget = calcExplosionDamage(target, crystalCenter);
                    double dmgToSelf   = calcExplosionDamage(mc.player, crystalCenter);

                    if (dmgToTarget < minDamage.get()) continue;
                    if (dmgToSelf   > maxSelfDamage.get()) continue;

                    // Score: maximise enemy damage, minimise self-damage
                    double score = dmgToTarget - dmgToSelf * 0.5;
                    if (score > bestScore) {
                        bestScore = score;
                        bestPos   = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private void placeCrystal(BlockPos basePos) {
        BlockPos above = basePos.up();
        // Place on top face of the base block
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

    // -------------------------------------------------------------------------
    // Crystal breaking
    // -------------------------------------------------------------------------

    private EndCrystalEntity findBestCrystalToBreak() {
        EndCrystalEntity best     = null;
        double            bestDmg = Double.NEGATIVE_INFINITY;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (EntityUtil.distanceTo(crystal) > breakRange.get()) continue;

            // Find nearest target to this crystal
            PlayerEntity nearestTarget = findNearestTarget();
            if (nearestTarget == null) continue;

            Vec3d crystalPos = crystal.getPos().add(0, 1, 0);
            double dmg = calcExplosionDamage(nearestTarget, crystalPos);

            if (dmg < minDamage.get()) continue;
            double selfDmg = calcExplosionDamage(mc.player, crystalPos);
            if (selfDmg > maxSelfDamage.get()) continue;

            if (dmg > bestDmg) {
                bestDmg = dmg;
                best    = crystal;
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
        mc.player.swingMainHand();
    }

    // -------------------------------------------------------------------------
    // Damage calculation
    // -------------------------------------------------------------------------

    /**
     * Approximates the explosion damage dealt to {@code entity} by a crystal explosion
     * centered at {@code explosionPos}, using the vanilla explosion damage formula.
     *
     * <p>Simplified (no block exposure ray-cast for performance):
     * <pre>
     *   exposure = 1 - (dist / radius)
     *   impact   = exposure * (0.7 + radius * 0.3)
     *   damage   = (impact^2 + impact) * 7 * radius + 1
     * </pre>
     */
    private double calcExplosionDamage(net.minecraft.entity.LivingEntity entity, Vec3d explosionPos) {
        double dist = entity.getPos().add(0, entity.getHeight() / 2.0, 0)
                .distanceTo(explosionPos);
        if (dist > CRYSTAL_RADIUS) return 0;

        double exposure = 1.0 - (dist / CRYSTAL_RADIUS);
        double impact   = exposure * (0.7 + CRYSTAL_RADIUS * 0.3);
        return (impact * impact + impact) * 7.0 * CRYSTAL_RADIUS + 1.0;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PlayerEntity findNearestTarget() {
        if (mc.world == null) return null;
        PlayerEntity nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isDead() || p.getHealth() <= 0) continue;
            double d = EntityUtil.distanceTo(p);
            if (d < minDist) {
                minDist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private boolean holdingCrystal() {
        return mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL;
    }

    private void switchToCrystal() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }
}
