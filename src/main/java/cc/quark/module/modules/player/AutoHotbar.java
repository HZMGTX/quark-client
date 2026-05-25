package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoHotbar extends Module {

    private final BoolSetting bestTool = register(new BoolSetting(
            "Best Tool", "Switch to the fastest mining tool for the targeted block", true));

    public AutoHotbar() {
        super("AutoHotbar", "Selects the optimal hotbar tool for the current activity", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!bestTool.isEnabled()) return;
        if (mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.isAir()) return;

        float best = -1f;
        int   bestSlot = mc.player.getInventory().selectedSlot;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > best) { best = speed; bestSlot = i; }
        }

        mc.player.getInventory().selectedSlot = bestSlot;
    }
}
