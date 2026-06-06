package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
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

/**
 * SurroundPlus - enhanced defensive surround that places obsidian blocks
 * around the player. Extends vanilla Surround with upper-layer cage, diagonal
 * corners, and a configurable block-placement rate.
 */
public class SurroundPlus extends Module {

    private final BoolSetting center = register(new BoolSetting(
            "Center", "Snap player to block center before placing", true));

    private final BoolSetting upperLayer = register(new BoolSetting(
            "Upper Layer", "Also place blocks at Y+1 for a full cage", false));

    private final BoolSetting corners = register(new BoolSetting(
            "Corners", "Fill diagonal corner positions", false));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only place while on the ground", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between individual placements (0 = all at once)", 0, 0, 5));

    private int delayTicks = 0;
    private int placeIndex = 0;

    public SurroundPlus() {
        super("SurroundPlus", "Places obsidian around yourself defensively (enhanced)", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        delayTicks = 0;
        placeIndex = 0;
        if (center.isEnabled() && mc.player != null) {
            BlockPos feet = mc.player.getBlockPos();
            mc.player.setPosition(feet.getX() + 0.5, mc.player.getY(), feet.getZ() + 0.5);
        }
    }

    @Override
    public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        if (delayTicks > 0) { delayTicks--; return; }

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        List<BlockPos> targets = buildTargets();
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        if (delay.get() > 0) {
            if (placeIndex >= targets.size()) placeIndex = 0;
            while (placeIndex < targets.size()) {
                BlockPos pos = targets.get(placeIndex++);
                if (tryPlace(pos)) { delayTicks = delay.get(); break; }
            }
        } else {
            for (BlockPos pos : targets) tryPlace(pos);
        }

        mc.player.getInventory().selectedSlot = prev;
    }

    private List<BlockPos> buildTargets() {
        BlockPos feet = mc.player.getBlockPos();
        List<BlockPos> list = new ArrayList<>();
        int[][] cardinals = {{0,1},{0,-1},{1,0},{-1,0}};
        int[][] diagonals = {{1,1},{-1,1},{1,-1},{-1,-1}};

        for (int[] o : cardinals) list.add(feet.add(o[0], 0, o[1]));
        if (corners.isEnabled()) for (int[] o : diagonals) list.add(feet.add(o[0], 0, o[1]));
        if (upperLayer.isEnabled()) {
            for (int[] o : cardinals) list.add(feet.add(o[0], 1, o[1]));
            if (corners.isEnabled()) for (int[] o : diagonals) list.add(feet.add(o[0], 1, o[1]));
        }
        return list;
    }

    private boolean tryPlace(BlockPos target) {
        if (mc.world == null) return false;
        if (!mc.world.getBlockState(target).isAir()) return false;

        Direction face = findSupport(target);
        if (face == null) return false;

        BlockPos neighbor = target.offset(face);
        Vec3d hit = Vec3d.ofCenter(target).add(
                face.getOffsetX() * 0.5,
                face.getOffsetY() * 0.5,
                face.getOffsetZ() * 0.5);

        BlockHitResult result = new BlockHitResult(hit, face.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        return true;
    }

    private Direction findSupport(BlockPos target) {
        for (Direction d : Direction.values()) {
            BlockPos n = target.offset(d);
            var state = mc.world.getBlockState(n);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, n)) return d;
        }
        return null;
    }

    private int findBlockSlot() {
        int fallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem)) continue;
            if (s.getItem() == Items.OBSIDIAN) return i;
            if (fallback == -1) fallback = i;
        }
        return fallback;
    }
}
