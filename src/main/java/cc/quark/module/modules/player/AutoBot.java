package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoBot - a simple scripted bot that performs automated tasks.
 *
 * MineBot:  moves forward and mines blocks in front of the player.
 * FarmBot:  finds nearby harvestable crops and harvests them.
 * GrindBot: locates the nearest hostile mob and attacks it.
 */
public class AutoBot extends Module {

    private final ModeSetting botMode = register(new ModeSetting(
            "Mode", "Bot behavior mode", "MineBot",
            "MineBot", "FarmBot", "GrindBot"));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search/attack range", 4.0, 2.0, 8.0));

    private final BoolSetting autoEat = register(new BoolSetting(
            "Auto Eat", "Eat food when hungry", true));

    // Eat threshold (hunger <= 14 out of 20)
    private static final int EAT_THRESHOLD = 14;

    public AutoBot() {
        super("AutoBot", "Basic scripted bot for mining, farming, and grinding", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Auto-eat takes priority over bot actions
        if (autoEat.isEnabled() && mc.player.getHungerManager().getFoodLevel() <= EAT_THRESHOLD) {
            tryEat();
            return;
        }

        switch (botMode.get()) {
            case "MineBot"  -> tickMineBot();
            case "FarmBot"  -> tickFarmBot();
            case "GrindBot" -> tickGrindBot();
        }
    }

    // -------------------------------------------------------------------------
    // MineBot
    // -------------------------------------------------------------------------

    private void tickMineBot() {
        // Hold forward movement
        mc.options.forwardKey.setPressed(true);

        // Find the block directly in front of the player and mine it
        Vec3d look = mc.player.getRotationVector();
        BlockPos target = mc.player.getBlockPos().add(
                (int) Math.round(look.x), 0, (int) Math.round(look.z));

        BlockPos[] candidates = {target, target.up(), target.down()};
        for (BlockPos pos : candidates) {
            if (mc.world.getBlockState(pos).isAir()) continue;
            Block block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.BEDROCK) continue;

            mc.interactionManager.attackBlock(pos, Direction.NORTH);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Send stop_destroy after start to try instant break
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.NORTH));
            break;
        }
    }

    // -------------------------------------------------------------------------
    // FarmBot
    // -------------------------------------------------------------------------

    private void tickFarmBot() {
        double r = range.get();
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos bestCrop = null;
        double   bestDist = Double.MAX_VALUE;

        // Find nearest fully grown crop
        for (BlockPos pos : BlockPos.iterate(
                (int)(playerPos.getX() - r), (int)(playerPos.getY() - r), (int)(playerPos.getZ() - r),
                (int)(playerPos.getX() + r), (int)(playerPos.getY() + r), (int)(playerPos.getZ() + r))) {
            var state = mc.world.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop)) continue;
            if (!crop.isMature(state)) continue;
            double d = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
            if (d < bestDist) {
                bestDist = d;
                bestCrop = pos.toImmutable();
            }
        }

        if (bestCrop == null) return;

        // Move toward crop if too far to harvest
        Vec3d cropCenter = Vec3d.ofCenter(bestCrop);
        if (bestDist > 2.5) {
            Vec3d dir = cropCenter.subtract(mc.player.getPos()).normalize();
            mc.player.setVelocity(dir.x * 0.2, mc.player.getVelocity().y, dir.z * 0.2);
        } else {
            // Harvest by left-clicking the block
            mc.interactionManager.attackBlock(bestCrop, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // -------------------------------------------------------------------------
    // GrindBot
    // -------------------------------------------------------------------------

    private void tickGrindBot() {
        double r = range.get();
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof HostileEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(living);
            if (d > r) continue;
            if (d < nearestDist) {
                nearestDist = d;
                nearest = living;
            }
        }

        if (nearest == null) return;

        // Switch to best sword in hotbar
        int swordSlot = findBestSwordSlot();
        if (swordSlot != -1) mc.player.getInventory().selectedSlot = swordSlot;

        // Move toward target if too far away
        if (nearestDist > 2.0) {
            Vec3d dir = nearest.getPos().subtract(mc.player.getPos()).normalize();
            mc.player.setVelocity(dir.x * 0.25, mc.player.getVelocity().y, dir.z * 0.25);
        } else {
            // Attack
            mc.interactionManager.attackEntity(mc.player, nearest);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void tryEat() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!stack.isFood()) continue;
            mc.player.getInventory().selectedSlot = i;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            return;
        }
    }

    private int findBestSwordSlot() {
        int best = -1;
        float bestDmg = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem sword) {
                float dmg = sword.getMaterial().getAttackDamage();
                if (dmg > bestDmg) {
                    bestDmg = dmg;
                    best    = i;
                }
            }
        }
        return best;
    }
}
