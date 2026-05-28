package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoSelect extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Selection strategy", "Smart", "Smart", "Aggressive"));
    private final BoolSetting switchBack = register(new BoolSetting("Switch Back", "Return to original slot after switching", false));

    private int savedSlot = -1;

    public AutoSelect() {
        super("AutoSelect", "Auto-selects best tool or weapon for current target", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.crosshairTarget == null) {
            restoreSlot();
            return;
        }

        if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
            if (ehr.getEntity() instanceof LivingEntity || mode.is("Aggressive")) {
                int slot = InventoryUtil.findBestSword();
                if (slot >= 0 && slot < 9) {
                    saveAndSwitch(slot);
                }
            }
        } else if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) mc.crosshairTarget;
            BlockState state = mc.world.getBlockState(bhr.getBlockPos());
            if (!state.isAir()) {
                int bestSlot = mc.player.getInventory().selectedSlot;
                float bestSpeed = -1f;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    float speed = stack.getMiningSpeedMultiplier(state);
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        bestSlot = i;
                    }
                }
                saveAndSwitch(bestSlot);
            }
        } else {
            restoreSlot();
        }
    }

    private void saveAndSwitch(int slot) {
        if (switchBack.isEnabled() && savedSlot == -1) {
            savedSlot = mc.player.getInventory().selectedSlot;
        }
        mc.player.getInventory().selectedSlot = slot;
    }

    private void restoreSlot() {
        if (switchBack.isEnabled() && savedSlot != -1) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }
}
