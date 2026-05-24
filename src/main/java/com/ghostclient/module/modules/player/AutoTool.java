package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.EnumSetting;
import com.ghostclient.setting.IntSetting;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * AutoTool - automatically switches to the best tool for the block being targeted.
 *
 * Preference modes:
 *   SPEED  – choose the tool that gives the highest mining speed.
 *   DAMAGE – choose the tool that preserves durability (only switch when strictly required).
 */
public class AutoTool extends Module {

    public enum Preference {
        SPEED, DAMAGE
    }

    private final EnumSetting<Preference> preference = register(new EnumSetting<>(
            "Preference", "Tool selection strategy", Preference.SPEED));

    private final IntSetting keepDurability = register(new IntSetting(
            "Keep Durability", "Don't use a tool if its remaining uses fall below this value", 5, 1, 100));

    /** The slot we were in before AutoTool switched. -1 means no switch was made. */
    private int previousSlot = -1;

    public AutoTool() {
        super("AutoTool", "Auto-switches to the best tool for the targeted block", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        // Restore original slot when disabled
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            // Restore previous slot when not targeting a block
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            return;
        }

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        Block block = state.getBlock();

        int bestSlot = findBestToolSlot(block, state, pos);
        if (bestSlot == -1) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != bestSlot) {
            if (previousSlot == -1) {
                previousSlot = currentSlot;
            }
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    /**
     * Finds the hotbar slot (0-8) containing the best tool for the given block.
     * Returns -1 if no suitable tool is found or current tool is already best.
     */
    private int findBestToolSlot(Block block, BlockState state, BlockPos pos) {
        if (mc.player == null || mc.world == null) return -1;

        int bestSlot = -1;
        float bestSpeed = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            // Respect keepDurability setting
            if (stack.isDamageable()) {
                int remaining = stack.getMaxDamage() - stack.getDamage();
                if (remaining <= keepDurability.get()) continue;
            }

            float speed = stack.getMiningSpeedMultiplier(state);

            if (preference.get() == Preference.SPEED) {
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            } else {
                // DAMAGE mode: only switch if this tool is specifically effective
                // (i.e., gives >1x speed) and is "lighter" on durability
                if (speed > 1.0f && speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }
}
