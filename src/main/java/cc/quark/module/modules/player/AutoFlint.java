package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoFlint extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to look for gravel blocks", 3.0, 1.0, 6.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between mining attempts", 300, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoFlint() {
        super("AutoFlint", "Creates flint from gravel automatically", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find gravel in range
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) range.get();
        BlockPos gravelPos = null;

        outer:
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos candidate = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(candidate).getBlock() == Blocks.GRAVEL) {
                        if (mc.player.getPos().distanceTo(candidate.toCenterPos()) <= range.get()) {
                            gravelPos = candidate;
                            break outer;
                        }
                    }
                }
            }
        }

        if (gravelPos == null) return;

        // Find a shovel or any tool
        int toolSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.IRON_SHOVEL
                    || mc.player.getInventory().getStack(i).getItem() == Items.DIAMOND_SHOVEL
                    || mc.player.getInventory().getStack(i).getItem() == Items.NETHERITE_SHOVEL
                    || mc.player.getInventory().getStack(i).getItem() == Items.STONE_SHOVEL
                    || mc.player.getInventory().getStack(i).getItem() == Items.WOODEN_SHOVEL
                    || mc.player.getInventory().getStack(i).getItem() == Items.GOLDEN_SHOVEL) {
                toolSlot = i;
                break;
            }
        }

        int prev = mc.player.getInventory().selectedSlot;
        if (toolSlot != -1) mc.player.getInventory().selectedSlot = toolSlot;

        // Attack (mine) the gravel block
        mc.interactionManager.attackBlock(gravelPos, Direction.UP);
        mc.options.attackKey.setPressed(true);

        mc.player.getInventory().selectedSlot = prev;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.attackKey.setPressed(false);
    }
}
