package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoMushroom extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to scan for mushrooms to bonemeal", 5, 1, 8));
    private final BoolSetting harvest = register(new BoolSetting(
            "Harvest", "Break mature mushroom blocks", true));
    private final BoolSetting warnEmpty = register(new BoolSetting(
            "Warn Empty", "Warn when out of bone meal", true));
    private final TimerUtil timer = new TimerUtil();

    private static final Set<Block> MUSHROOMS = new HashSet<>(Arrays.asList(
            Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM
    ));

    public AutoMushroom() {
        super("AutoMushroom", "Farms mushrooms with bone meal automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;

        int r = radius.get();
        BlockPos center = mc.player.getBlockPos();

        // Harvest large mushroom blocks first
        if (harvest.isEnabled()) {
            for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, r + 4, r))) {
                if (pos.getSquaredDistance(mc.player.getPos()) > r * r) continue;
                Block b = mc.world.getBlockState(pos).getBlock();
                if (b == Blocks.RED_MUSHROOM_BLOCK || b == Blocks.BROWN_MUSHROOM_BLOCK
                        || b == Blocks.MUSHROOM_STEM) {
                    mc.interactionManager.attackBlock(pos, Direction.UP);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    timer.reset();
                    return;
                }
            }
        }

        // Apply bone meal to small mushrooms
        int boneMealSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.BONE_MEAL)) {
                boneMealSlot = i;
                break;
            }
        }
        if (boneMealSlot == -1) {
            if (warnEmpty.isEnabled()) ChatUtil.warn("[AutoMushroom] No bone meal in hotbar.");
            disable();
            return;
        }

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > r * r) continue;
            if (!MUSHROOMS.contains(mc.world.getBlockState(pos).getBlock())) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = boneMealSlot;
            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }
    }
}
