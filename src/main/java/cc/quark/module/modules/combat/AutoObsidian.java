package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoObsidian — when the player's health falls below the threshold, places
 * obsidian on the 4 cardinal blocks adjacent to the player.
 */
public class AutoObsidian extends Module {

    private final IntSetting hpThreshold = register(new IntSetting("HPThreshold", "Health below which to place obsidian", 8,   1,  20));
    private final IntSetting delay       = register(new IntSetting("Delay",        "Ms between placements",               100, 50, 500));

    private final TimerUtil timer    = new TimerUtil();
    private int             prevSlot = -1;
    private int             placeIdx = 0;

    private static final Direction[] CARDINALS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public AutoObsidian() {
        super("AutoObsidian", "Places obsidian around the player when health is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
        placeIdx = 0;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() >= hpThreshold.get()) return;
        if (!timer.hasReached(delay.get())) return;

        int obsSlot = findObsidian();
        if (obsSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != obsSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = obsSlot;
        }

        Direction dir = CARDINALS[placeIdx % CARDINALS.length];
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos target    = playerPos.offset(dir);

        if (mc.world.getBlockState(target).isAir()) {
            BlockPos support = target.down();
            if (!mc.world.getBlockState(support).isAir()) {
                BlockHitResult hit = new BlockHitResult(
                        Vec3d.ofCenter(support).add(0, 0.5, 0),
                        Direction.UP,
                        support,
                        false
                );
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                timer.reset();
            }
        }

        placeIdx = (placeIdx + 1) % CARDINALS.length;
        restoreSlot();
    }

    private int findObsidian() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() instanceof BlockItem bi && bi.getBlock() == Blocks.OBSIDIAN) {
                return i;
            }
        }
        return -1;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
