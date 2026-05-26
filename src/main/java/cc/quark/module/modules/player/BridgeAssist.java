package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
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

public class BridgeAssist extends Module {

    private final BoolSetting autoSneak = register(new BoolSetting(
            "Auto Sneak", "Automatically sneak when approaching a block edge while holding blocks", true));

    private final BoolSetting tower = register(new BoolSetting(
            "Tower", "Place blocks downward automatically when sneaking at an edge", false));

    public BridgeAssist() {
        super("BridgeAssist", "Helps prevent falling while bridging", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.sneakKey.setPressed(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean holdingBlock = isHoldingBlock();

        if (autoSneak.isEnabled() && holdingBlock && mc.player.isOnGround()) {
            if (isAtBlockEdge()) {
                mc.options.sneakKey.setPressed(true);
            } else {
                mc.options.sneakKey.setPressed(false);
            }
        }

        if (tower.isEnabled() && mc.player.isSneaking() && holdingBlock) {
            placeBlockBelow();
        }
    }

    private boolean isAtBlockEdge() {
        if (mc.player == null || mc.world == null) return false;

        double yaw = Math.toRadians(mc.player.getYaw());
        double forwardX = -Math.sin(yaw) * 0.6;
        double forwardZ = Math.cos(yaw) * 0.6;

        double nextX = mc.player.getX() + forwardX;
        double nextZ = mc.player.getZ() + forwardZ;
        double currentY = mc.player.getY();

        BlockPos belowNext = new BlockPos(
                (int) Math.floor(nextX),
                (int) Math.floor(currentY) - 1,
                (int) Math.floor(nextZ));

        return mc.world.getBlockState(belowNext).isAir();
    }

    private boolean isHoldingBlock() {
        if (mc.player == null) return false;
        ItemStack held = mc.player.getMainHandStack();
        return held.getItem() instanceof BlockItem;
    }

    private boolean placeBlockBelow() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return false;

        BlockPos pos = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(pos).isAir()) return false;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = mc.world.getBlockState(neighborPos);
            if (neighborState.isAir() || neighborState.getBlock() instanceof FallingBlock) continue;

            Direction placeDir = dir.getOpposite();
            Vec3d hitVec = Vec3d.ofCenter(neighborPos).add(
                    Vec3d.of(placeDir.getVector()).multiply(0.5));

            float oldYaw = mc.player.getYaw();
            float oldPitch = mc.player.getPitch();
            mc.player.setPitch(89.9f);

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
}
