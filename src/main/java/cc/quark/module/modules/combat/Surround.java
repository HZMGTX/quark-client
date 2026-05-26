package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Surround extends Module {

    private final BoolSetting center = register(new BoolSetting(
            "Center", "Snap player to block center before placing", true));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only place while the player is on the ground", true));

    private final BoolSetting burrow = register(new BoolSetting(
            "Burrow", "Also place a block at the player's exact foot position", false));

    private final BoolSetting extend = register(new BoolSetting(
            "Extend", "Place an additional ring at Y+1 for a full cage (8 blocks)", false));

    private final BoolSetting fillCorners = register(new BoolSetting(
            "Fill Corners", "Also fill the 4 diagonal corner positions", false));

    private final BoolSetting clearOnDisable = register(new BoolSetting(
            "Clear On Disable", "Remove placed surround blocks when disabling", false));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between individual block placements (0 = all at once)", 0, 0, 5));

    private int delayTicks = 0;
    private int placeIndex = 0;
    private final List<BlockPos> placedBlocks = new ArrayList<>();

    private long lastSecondMs = 0;
    private int placedThisSecond = 0;
    private int displayBps = 0;

    public Surround() {
        super("Surround", "Places an obsidian surround to protect against crystal damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        delayTicks = 0;
        placeIndex = 0;
        placedBlocks.clear();
        placedThisSecond = 0;
        displayBps = 0;
        lastSecondMs = System.currentTimeMillis();

        if (center.isEnabled()) {
            BlockPos feet = mc.player.getBlockPos();
            mc.player.setPosition(feet.getX() + 0.5, mc.player.getY(), feet.getZ() + 0.5);
        }
    }

    @Override
    public void onDisable() {
        if (clearOnDisable.isEnabled() && mc.player != null && mc.world != null && mc.interactionManager != null) {
            for (BlockPos bp : placedBlocks) {
                BlockState state = mc.world.getBlockState(bp);
                if (!state.isAir()) {
                    mc.interactionManager.attackBlock(bp, Direction.UP);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
        placedBlocks.clear();
    }

    @Override
    public String getSuffix() {
        return displayBps + " bps";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        long now = System.currentTimeMillis();
        if (now - lastSecondMs >= 1000L) {
            displayBps = placedThisSecond;
            placedThisSecond = 0;
            lastSecondMs = now;
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        List<BlockPos> positions = buildPositions();

        int blockSlot = findBestBlockSlot();
        if (blockSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        if (delay.get() > 0) {
            if (placeIndex >= positions.size()) placeIndex = 0;
            while (placeIndex < positions.size()) {
                BlockPos target = positions.get(placeIndex);
                placeIndex++;
                if (tryPlace(target)) {
                    delayTicks = delay.get();
                    break;
                }
            }
        } else {
            placeIndex = 0;
            for (BlockPos target : positions) {
                tryPlace(target);
            }
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private boolean tryPlace(BlockPos target) {
        if (mc.world == null) return false;
        BlockState existing = mc.world.getBlockState(target);
        if (!existing.isAir() && !existing.isReplaceable()) return false;

        Direction placeDir = findSupportFace(target);
        if (placeDir == null) return false;

        BlockPos neighbor = target.offset(placeDir);
        Vec3d hitVec = Vec3d.ofCenter(target).add(
                placeDir.getOffsetX() * 0.5,
                placeDir.getOffsetY() * 0.5,
                placeDir.getOffsetZ() * 0.5);

        BlockHitResult hitResult = new BlockHitResult(hitVec, placeDir.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        placedBlocks.add(target);
        placedThisSecond++;
        return true;
    }

    private List<BlockPos> buildPositions() {
        BlockPos feetPos = mc.player.getBlockPos();
        List<BlockPos> result = new ArrayList<>();

        int[][] cardinals = { {0, 1}, {0, -1}, {1, 0}, {-1, 0} };
        int[][] corners   = { {1, 1}, {-1, 1}, {1, -1}, {-1, -1} };

        for (int[] o : cardinals) result.add(feetPos.add(o[0], 0, o[1]));

        if (fillCorners.isEnabled()) {
            for (int[] o : corners) result.add(feetPos.add(o[0], 0, o[1]));
        }

        if (extend.isEnabled()) {
            for (int[] o : cardinals) result.add(feetPos.add(o[0], 1, o[1]));
            if (fillCorners.isEnabled()) {
                for (int[] o : corners) result.add(feetPos.add(o[0], 1, o[1]));
            }
        }

        if (burrow.isEnabled()) {
            result.add(0, feetPos);
        }

        return result;
    }

    private Direction findSupportFace(BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, neighbor)) {
                return dir;
            }
        }
        return null;
    }

    private int findBestBlockSlot() {
        int fallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) continue;
            if (stack.getItem() == Items.OBSIDIAN) return i;
            if (fallback == -1) fallback = i;
        }
        return fallback;
    }
}
