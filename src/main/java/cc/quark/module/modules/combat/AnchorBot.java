package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AnchorBot extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to place/charge anchors", 4.0, 1.0, 6.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 300, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AnchorBot() {
        super("AnchorBot", "Auto-places and charges respawn anchors", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Look for an anchor block in range to charge (right-click with glowstone)
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    double dist = mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));
                    if (dist > range.get() * range.get()) continue;

                    if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
                        // Try to charge with glowstone
                        int glowstoneSlot = findItemInHotbar(Items.GLOWSTONE);
                        if (glowstoneSlot >= 0) {
                            mc.player.getInventory().selectedSlot = glowstoneSlot;
                            BlockHitResult hit = new BlockHitResult(
                                    Vec3d.ofCenter(pos), Direction.UP, pos, false);
                            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                            timer.reset();
                            return;
                        }
                    }
                }
            }
        }

        // No anchor found — try placing one from inventory
        int anchorSlot = findItemInHotbar(Items.RESPAWN_ANCHOR);
        if (anchorSlot >= 0) {
            mc.player.getInventory().selectedSlot = anchorSlot;
            // Place at feet
            BlockPos below = playerPos.down();
            if (mc.world.getBlockState(below).isSolidBlock(mc.world, below)) {
                BlockHitResult hit = new BlockHitResult(
                        Vec3d.ofCenter(playerPos), Direction.DOWN, playerPos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                timer.reset();
            }
        }
    }

    private int findItemInHotbar(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }
}
