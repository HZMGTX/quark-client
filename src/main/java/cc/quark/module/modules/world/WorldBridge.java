package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WorldBridge extends Module {

    private final BoolSetting sneak = register(new BoolSetting(
            "Sneak", "Automatically sneak while bridging", true));

    private final BoolSetting requireSneaking = register(new BoolSetting(
            "RequireSneak", "Only bridge when player is already sneaking", false));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between block placements", 50, 0, 500));

    private final BoolSetting onlyMoving = register(new BoolSetting(
            "OnlyMoving", "Only place blocks while the player is moving", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBridge() {
        super("WorldBridge", "Automatically places blocks behind the player while walking forward", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @Override
    public void onDisable() {
        // Release sneak if we were forcing it
        if (mc.player != null && sneak.isEnabled()) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Require sneak option
        if (requireSneaking.isEnabled() && !mc.player.isSneaking()) return;

        // Check if player is moving
        if (onlyMoving.isEnabled()) {
            double vx = mc.player.getVelocity().x;
            double vz = mc.player.getVelocity().z;
            if (Math.abs(vx) < 0.01 && Math.abs(vz) < 0.01) return;
        }

        // Auto-sneak to avoid falling off the edge
        if (sneak.isEnabled()) {
            mc.options.sneakKey.setPressed(true);
        }

        // Find a block item in hotbar
        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        // The block behind the player (one block below feet, in opposite movement direction)
        BlockPos feetPos = mc.player.getBlockPos();
        BlockPos belowFeet = feetPos.down();

        // Only place if there is no block below the player's feet
        if (!mc.world.getBlockState(belowFeet).isAir()) return;

        // Find a solid block adjacent to the air gap to place on
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN}) {
            BlockPos neighborPos = belowFeet.offset(dir);
            BlockState neighborState = mc.world.getBlockState(neighborPos);
            if (!neighborState.isAir() && !neighborState.isLiquid()) {
                // Place on the face of this neighbor that faces back toward belowFeet
                Direction placeDir = dir.getOpposite();
                Vec3d hitVec = Vec3d.ofCenter(belowFeet);
                BlockHitResult hit = new BlockHitResult(hitVec, placeDir, neighborPos, false);

                int savedSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = blockSlot;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = savedSlot;

                timer.reset();
                return;
            }
        }

        timer.reset();
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }
}
