package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class AutoSwitchTool extends Module {

    private final BoolSetting silk = register(new BoolSetting(
            "Silk Touch", "Prefer tools with Silk Touch enchantment", false));

    private final BoolSetting fortune = register(new BoolSetting(
            "Fortune", "Prefer tools with Fortune enchantment", true));

    private int previousSlot = -1;

    public AutoSwitchTool() {
        super("AutoSwitchTool", "Switches to best tool for block being mined", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            return;
        }

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        int best = findBestSlot(state);
        if (best == -1) return;

        int current = mc.player.getInventory().selectedSlot;
        if (current != best) {
            if (previousSlot == -1) previousSlot = current;
            mc.player.getInventory().selectedSlot = best;
        }
    }

    private int findBestSlot(BlockState state) {
        int bestSlot = -1;
        float bestSpeed = -1f;
        boolean bestHasSilk = false;
        boolean bestHasFortune = false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed <= 1.0f) continue;

            boolean hasSilk = hasEnchant(stack, "silk_touch");
            boolean hasFortune = hasEnchant(stack, "fortune");

            boolean better = false;
            if (speed > bestSpeed) {
                better = true;
            } else if (speed == bestSpeed) {
                if (silk.isEnabled() && hasSilk && !bestHasSilk) better = true;
                else if (fortune.isEnabled() && hasFortune && !bestHasFortune) better = true;
            }

            if (better) {
                bestSpeed = speed;
                bestSlot = i;
                bestHasSilk = hasSilk;
                bestHasFortune = hasFortune;
            }
        }
        return bestSlot;
    }

    private boolean hasEnchant(ItemStack stack, String enchantId) {
        var enchants = stack.getEnchantments();
        for (var entry : enchants.getEnchantmentEntries()) {
            if (entry.getKey().map(k -> k.getIdAsString().contains(enchantId)).orElse(false)) {
                return true;
            }
        }
        return false;
    }
}
