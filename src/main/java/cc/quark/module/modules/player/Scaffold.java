package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

    private final BoolSetting tower = register(new BoolSetting(
            "Tower", "Place blocks upward when holding jump (tower mode)", true));

    private final BoolSetting safeWalk = register(new BoolSetting(
            "Safe Walk", "Prevent walking off edges by stopping at block boundaries", true));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Maintain sprint while scaffolding", true));

    private final BoolSetting diagonal = register(new BoolSetting(
            "Diagonal", "Allow diagonal block placement", false));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between placements (0 = no delay)", 1, 0, 10));

    private int delayTicks = 0;
    private boolean towerActive = false;

    public Scaffold() {
        super("Scaffold", "Auto-places blocks under player while walking in air", Category.PLAYER);
    }

    @Override
    public String getSuffix() {
        return towerActive ? "Tower" : null;
    }

    @Override
    public void onEnable() {
        delayTicks = 0;
        towerActive = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.sneakKey.setPressed(false);
        }
        towerActive = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Safe walk: sneak key to prevent falling off edges
        if (safeWalk.isEnabled()) {
            BlockPos belowNext = getPositionAhead().down();
            boolean edgeDanger = mc.world.getBlockState(belowNext).isAir();
            mc.options.sneakKey.setPressed(edgeDanger && mc.player.isOnGround());
        }

        // Sprint: maintain sprint while scaffolding
        if (sprint.isEnabled() && mc.player.isOnGround()) {
            if (!mc.player.isSneaking()) {
                mc.player.setSprinting(true);
            }
        }

        // Delay between placements
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) {
            towerActive = false;
            return;
        }

        int savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        boolean isTowering = tower.isEnabled() && mc.options.jumpKey.isPressed();
        towerActive = isTowering;

        if (isTowering) {
            // Tower mode: place block directly below and jump
            if (placeBelow()) {
                mc.player.jump();
                if (delay.get() > 0) delayTicks = delay.get();
            }
        } else {
            // Normal scaffold: place block below
            if (placeBelow()) {
                if (delay.get() > 0) {
                    delayTicks = delay.get();
                }
            }
        }

        mc.player.getInventory().selectedSlot = savedSlot;
    }

    private boolean placeBelow() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return false;

        BlockPos pos = mc.player.getBlockPos().down();

        if (!mc.world.getBlockState(pos).isAir()) return false;

        // Collect directions to check — include diagonals if enabled
        Direction[] dirs = diagonal.isEnabled()
                ? Direction.values()
                : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP};

        for (Direction dir : dirs) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = mc.world.getBlockState(neighborPos);

            if (neighborState.isAir() || neighborState.getBlock() instanceof FallingBlock) continue;

            Direction placeDir = dir.getOpposite();
            Vec3d hitVec = Vec3d.ofCenter(neighborPos).add(
                    Vec3d.of(placeDir.getVector()).multiply(0.5)
            );

            float targetYaw = getYawForDirection(dir);
            float targetPitch = (dir == Direction.DOWN) ? 89f : 45f;
            float oldYaw = mc.player.getYaw();
            float oldPitch = mc.player.getPitch();

            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);

            BlockHitResult hitResult = new BlockHitResult(hitVec, placeDir, neighborPos, false);
            ActionResult result = mc.interactionManager.interactBlock(
                    mc.player, Hand.MAIN_HAND, hitResult);

            mc.player.setYaw(oldYaw);
            mc.player.setPitch(oldPitch);

            if (result.isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
                return true;
            }
        }

        return false;
    }

    private BlockPos getPositionAhead() {
        if (mc.player == null) return BlockPos.ORIGIN;
        double yaw = Math.toRadians(mc.player.getYaw());
        double x = mc.player.getX() - Math.sin(yaw) * 0.6;
        double z = mc.player.getZ() + Math.cos(yaw) * 0.6;
        return new BlockPos((int) Math.floor(x), (int) Math.floor(mc.player.getY()), (int) Math.floor(z));
    }

    private int findBlockInHotbar() {
        if (mc.player == null) return -1;

        int bestSlot = -1;
        int bestCount = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                if (!(blockItem.getBlock() instanceof FallingBlock)) {
                    if (stack.getCount() > bestCount) {
                        bestCount = stack.getCount();
                        bestSlot = i;
                    }
                } else if (bestSlot == -1) {
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    private float getYawForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST -> -90f;
            case EAST -> 90f;
            default -> mc.player != null ? mc.player.getYaw() : 0f;
        };
    }
}
