package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
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
import net.minecraft.world.World;

public class AnchorPlace extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to find targets in blocks", 5, 2, 8));

    private final TimerUtil timer = new TimerUtil();
    private boolean awaitingCharge = false;
    private BlockPos anchorPos = null;

    public AnchorPlace() {
        super("AnchorPlace", "Auto-places respawn anchors and charges them in the Nether", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        awaitingCharge = false;
        anchorPos = null;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.world.getRegistryKey() != World.NETHER) return;
        if (!timer.hasReached(200)) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) {
            awaitingCharge = false;
            anchorPos = null;
            return;
        }

        if (!awaitingCharge) {
            int anchorSlot = findSlot(Items.RESPAWN_ANCHOR);
            if (anchorSlot == -1) return;

            BlockPos placePos = findAirNear(target);
            if (placePos == null) return;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = anchorSlot;

            Direction face = Direction.UP;
            Vec3d hitVec = Vec3d.ofCenter(placePos);
            BlockHitResult hit = new BlockHitResult(hitVec, face, placePos.down(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            mc.player.getInventory().selectedSlot = prevSlot;
            anchorPos = placePos;
            awaitingCharge = true;
        } else {
            if (anchorPos == null) { awaitingCharge = false; return; }
            if (mc.world.getBlockState(anchorPos).getBlock() != Blocks.RESPAWN_ANCHOR) {
                awaitingCharge = false;
                anchorPos = null;
                return;
            }
            int glowSlot = findSlot(Items.GLOWSTONE);
            if (glowSlot != -1) {
                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = glowSlot;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anchorPos), Direction.UP, anchorPos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.getInventory().selectedSlot = prevSlot;
            }
        }

        timer.reset();
    }

    private BlockPos findAirNear(PlayerEntity target) {
        BlockPos base = target.getBlockPos();
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos candidate = base.offset(dir);
            if (mc.world.getBlockState(candidate).isAir()
                    && mc.world.getBlockState(candidate.up()).isAir()
                    && mc.player.getEyePos().distanceTo(Vec3d.ofCenter(candidate)) <= range.get()) {
                return candidate;
            }
        }
        return null;
    }

    private int findSlot(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(item)) return i;
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
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
