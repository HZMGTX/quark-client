package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class BestTool extends Module {

    private final BoolSetting onlyWhenMining = register(new BoolSetting(
            "Only Mining", "Only swap when attack key is held", true));
    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Restore previous slot when not mining", true));

    private int prevSlot = -1;

    public BestTool() {
        super("BestTool", "Always equips the best tool for the targeted block", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (restoreSlot.isEnabled() && prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            if (restoreSlot.isEnabled() && prevSlot >= 0) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            return;
        }

        if (onlyWhenMining.isEnabled() && !mc.options.attackKey.isPressed()) return;

        // Determine best tool by checking what the block needs
        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockState state = mc.world == null ? null : mc.world.getBlockState(pos);

        int best = -1;
        if (state != null) {
            best = findBestToolForBlock(state);
        }
        if (best < 0) best = InventoryUtil.findBestPickaxe();

        if (best >= 0 && best < 9) {
            if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = best;
        }
    }

    private int findBestToolForBlock(BlockState state) {
        float bestSpeed = 1f;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
