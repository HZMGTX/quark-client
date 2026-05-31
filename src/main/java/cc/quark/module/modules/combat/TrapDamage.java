package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TrapDamage extends Module {

    private final IntSetting duration = register(new IntSetting(
            "Duration", "Ticks before removing placed lava", 40, 5, 200));

    private final TimerUtil cycleTimer = new TimerUtil();

    private BlockPos lavaPos = null;
    private int lavaTicksLeft = 0;

    public TrapDamage() {
        super("TrapDamage", "Places lava bucket briefly near enemy then removes it", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        removeLava();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (lavaPos != null) {
            lavaTicksLeft--;
            if (lavaTicksLeft <= 0) {
                removeLava();
            }
            return;
        }

        if (!cycleTimer.hasReached(1000)) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        int lavaSlot = findLavaSlot();
        if (lavaSlot == -1) return;

        BlockPos placePos = findAirNear(target);
        if (placePos == null) return;

        Direction face = findSupportFace(placePos);
        if (face == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = lavaSlot;

        BlockPos neighbor = placePos.offset(face);
        Vec3d hitVec = Vec3d.ofCenter(placePos).add(
                face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(hitVec, face.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        mc.player.getInventory().selectedSlot = prevSlot;
        lavaPos = placePos;
        lavaTicksLeft = duration.get();
        cycleTimer.reset();
    }

    private void removeLava() {
        if (lavaPos == null || mc.world == null || mc.interactionManager == null) {
            lavaPos = null;
            return;
        }
        BlockState state = mc.world.getBlockState(lavaPos);
        if (state.getBlock() == Blocks.LAVA) {
            int bucketSlot = findBucketSlot();
            if (bucketSlot != -1) {
                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = bucketSlot;

                BlockHitResult hit = new BlockHitResult(
                        Vec3d.ofCenter(lavaPos), Direction.UP, lavaPos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

                mc.player.getInventory().selectedSlot = prevSlot;
            }
        }
        lavaPos = null;
    }

    private BlockPos findAirNear(PlayerEntity target) {
        BlockPos base = target.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockPos candidate = base.offset(dir);
            if (mc.world.getBlockState(candidate).isAir()
                    && mc.player.getEyePos().distanceTo(Vec3d.ofCenter(candidate)) <= 5.0) {
                return candidate;
            }
        }
        return null;
    }

    private Direction findSupportFace(BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && state.isSolidBlock(mc.world, neighbor)) return dir;
        }
        return null;
    }

    private int findLavaSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.LAVA_BUCKET)) return i;
        }
        return -1;
    }

    private int findBucketSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.BUCKET)) return i;
        }
        return -1;
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d <= 5.0 && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
