package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoComposter2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to find composters", 3.0, 1.0, 6.0));

    private long lastFill = 0;

    public AutoComposter2() {
        super("AutoComposter2", "Auto-fills composters with junk items", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastFill < 300) return;

        // Find compostable junk in inventory
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.BEETROOT_SEEDS
                    || stack.getItem() == Items.OAK_LEAVES || stack.getItem() == Items.GRASS) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        double r = range.get();
        BlockPos origin = mc.player.getBlockPos();
        for (int x = -(int)r; x <= (int)r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -(int)r; z <= (int)r; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() != Blocks.COMPOSTER) continue;
                    if (mc.player.distanceTo(Vec3d.ofCenter(pos)) > r) continue;

                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = slot;
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                            new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
                    mc.player.getInventory().selectedSlot = prev;
                    lastFill = System.currentTimeMillis();
                    return;
                }
            }
        }
    }
}
