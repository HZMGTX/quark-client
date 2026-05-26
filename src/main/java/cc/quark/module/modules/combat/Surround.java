package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Surround extends Module {

    private final BoolSetting center = register(new BoolSetting(
            "Center", "Snap player to the centre of the current block before placing", true));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only place blocks while the player is standing on the ground", true));

    private final BoolSetting feet = register(new BoolSetting(
            "Feet", "Place blocks at feet level (y+0) instead of only around player", true));

    private final BoolSetting extend = register(new BoolSetting(
            "Extend", "Extend surround 2 blocks wide in each direction", false));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between block placements (0 = instant all)", 0, 0, 10));

    private int delayTicks = 0;
    private int placementIndex = 0;

    public Surround() {
        super("Surround", "Places blocks around your feet to protect against crystals", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        delayTicks = 0;
        placementIndex = 0;

        if (center.isEnabled()) {
            BlockPos feet = mc.player.getBlockPos();
            double centreX = feet.getX() + 0.5;
            double centreZ = feet.getZ() + 0.5;
            mc.player.setPosition(centreX, mc.player.getY(), centreZ);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        // Build surround offsets based on settings
        int[][] cardinalOffsets = {
            {  0,  1 },  // North
            {  0, -1 },  // South
            {  1,  0 },  // East
            { -1,  0 },  // West
        };

        int[][] cornerOffsets = {
            {  1,  1 },  // NE
            { -1,  1 },  // NW
            {  1, -1 },  // SE
            { -1, -1 }   // SW
        };

        int[][] extendOffsets = {
            {  0,  2 },
            {  0, -2 },
            {  2,  0 },
            { -2,  0 },
        };

        java.util.List<int[]> allOffsets = new java.util.ArrayList<>();
        for (int[] o : cardinalOffsets) allOffsets.add(o);
        for (int[] o : cornerOffsets) allOffsets.add(o);
        if (extend.isEnabled()) {
            for (int[] o : extendOffsets) allOffsets.add(o);
        }

        BlockPos feetPos = mc.player.getBlockPos();
        int yOffset = feet.isEnabled() ? 0 : 0;

        int blockSlot = findBestBlockSlot(mc);
        if (blockSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        int placedThisTick = 0;
        for (int[] off : allOffsets) {
            BlockPos target = feetPos.add(off[0], yOffset, off[1]);
            BlockState existing = mc.world.getBlockState(target);
            if (!existing.isAir() && !existing.isReplaceable()) continue;

            Direction placeDir = findSupportFace(mc, target);
            if (placeDir == null) continue;

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

            placedThisTick++;

            // If delay is set, place one block per delay period
            if (delay.get() > 0) {
                delayTicks = delay.get();
                break;
            }
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private Direction findSupportFace(MinecraftClient mc, BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, neighbor)) {
                return dir;
            }
        }
        return null;
    }

    private int findBestBlockSlot(MinecraftClient mc) {
        int fallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem)) continue;
            if (stack.getItem() == Items.OBSIDIAN) return i;
            if (fallback == -1) fallback = i;
        }
        return fallback;
    }
}
