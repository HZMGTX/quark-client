package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoEndPortal extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to place eyes of ender in portal frames", 3.0, 1.0, 6.0));

    private long lastPlace = 0;

    public AutoEndPortal() {
        super("AutoEndPortal", "Automatically places eyes of ender in portal", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastPlace < 200) return;

        // Find eye of ender in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_EYE) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        double r = range.get();
        BlockPos origin = mc.player.getBlockPos();

        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (mc.player.distanceTo(Vec3d.ofCenter(pos)) > r) continue;
                    var state = mc.world.getBlockState(pos);
                    if (state.getBlock() == Blocks.END_PORTAL_FRAME
                            && !state.get(EndPortalFrameBlock.EYE)) {
                        int prev = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = slot;
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                                new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
                        mc.player.getInventory().selectedSlot = prev;
                        lastPlace = System.currentTimeMillis();
                        return;
                    }
                }
            }
        }
    }
}
