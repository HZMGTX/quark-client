package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * AutoTool - automatically switches to the best tool for the block being mined,
 * or to a sword when targeting a living entity.
 */
public class AutoTool extends Module {

    private final ModeSetting preference = register(new ModeSetting(
            "Preference", "Tool selection strategy", "Speed", "Speed", "Damage"));

    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Switch back to previous slot after mining", true));

    private final BoolSetting swordForMobs = register(new BoolSetting(
            "Sword for Mobs", "Switch to sword when targeting living entities", true));

    /** The slot we were in before AutoTool switched. -1 means no switch was made. */
    private int previousSlot = -1;

    public AutoTool() {
        super("AutoTool", "Auto-switches to the best tool for the targeted block or entity", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        restorePreviousSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        HitResult hit = mc.crosshairTarget;

        // Handle entity targeting (sword for mobs)
        if (hit != null && hit.getType() == HitResult.Type.ENTITY && swordForMobs.isEnabled()) {
            EntityHitResult ehr = (EntityHitResult) hit;
            if (ehr.getEntity() instanceof LivingEntity) {
                int swordSlot = findBestSwordSlot();
                if (swordSlot != -1) {
                    int current = mc.player.getInventory().selectedSlot;
                    if (current != swordSlot) {
                        if (previousSlot == -1) previousSlot = current;
                        mc.player.getInventory().selectedSlot = swordSlot;
                    }
                    return;
                }
            }
        }

        // Handle block targeting
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            if (switchBack.isEnabled()) restorePreviousSlot();
            return;
        }

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);

        int bestSlot = findBestToolSlot(state, pos);
        if (bestSlot == -1) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != bestSlot) {
            if (previousSlot == -1) previousSlot = currentSlot;
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private void restorePreviousSlot() {
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
    }

    /**
     * Finds the hotbar slot (0-8) with the best sword for combat.
     */
    private int findBestSwordSlot() {
        if (mc.player == null) return -1;
        int bestSlot = -1;
        float bestDmg = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem sword) {
                float dmg = sword.getMaterial().value().attackDamageBonus();
                if (dmg > bestDmg) {
                    bestDmg = dmg;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    /**
     * Finds the hotbar slot (0-8) containing the best tool for the given block.
     * Returns -1 if no suitable tool is found.
     */
    private int findBestToolSlot(BlockState state, BlockPos pos) {
        if (mc.player == null || mc.world == null) return -1;

        int bestSlot = -1;
        float bestSpeed = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            float speed = stack.getMiningSpeedMultiplier(state);

            if (preference.is("Speed")) {
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            } else {
                // Damage mode: only switch if specifically effective (>1x speed)
                if (speed > 1.0f && speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }
}
