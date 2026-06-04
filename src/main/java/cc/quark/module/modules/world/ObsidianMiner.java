package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ObsidianMiner extends Module {

    private final IntSetting speed = register(new IntSetting(
            "Speed", "Mining speed boost level (1-5)", 3, 1, 5));

    private int tickCount = 0;

    public ObsidianMiner() {
        super("ObsidianMiner", "Breaks obsidian faster with special timing", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Find diamond/netherite pickaxe in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIAMOND_PICKAXE
                    || stack.getItem() == Items.NETHERITE_PICKAXE) {
                mc.player.getInventory().selectedSlot = i;
                break;
            }
        }

        var hit = mc.crosshairTarget;
        if (!(hit instanceof net.minecraft.util.hit.BlockHitResult bhr)) return;
        BlockPos pos = bhr.getBlockPos();
        if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN) return;

        // Send repeated mining progress packets to simulate faster break
        tickCount++;
        if (tickCount % (6 - speed.get()) == 0) {
            mc.interactionManager.updateBlockBreakingProgress(pos, bhr.getSide());
        }
    }
}
