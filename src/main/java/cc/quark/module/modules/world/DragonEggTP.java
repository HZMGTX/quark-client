package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class DragonEggTP extends Module {
    public DragonEggTP() {
        super("Dragon Egg TP", "Assists in teleporting the dragon egg", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) { disable(); return; }
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.DRAGON_EGG) {
                        BlockPos below = pos.down();
                        int torchSlot = -1;
                        for (int i = 0; i < 9; i++) {
                            if (mc.player.getInventory().getStack(i).isOf(Items.TORCH)) { torchSlot = i; break; }
                        }
                        if (torchSlot == -1) { ChatUtil.warn("[DragonEgg] No torches found."); disable(); return; }
                        mc.player.getInventory().selectedSlot = torchSlot;
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                            new BlockHitResult(Vec3d.ofCenter(below), Direction.UP, below, false));
                        ChatUtil.info("[DragonEgg] Teleport triggered!");
                        disable(); return;
                    }
                }
            }
        }
        ChatUtil.warn("[DragonEgg] No dragon egg found nearby.");
        disable();
    }
}
