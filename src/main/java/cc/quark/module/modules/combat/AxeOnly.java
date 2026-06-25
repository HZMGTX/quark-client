package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;

/**
 * AxeOnly — cancels attacks when the player is not holding an axe.
 * If "Auto Switch" is on and an axe is found in the hotbar it is switched
 * to automatically so the attack still lands.
 */
public class AxeOnly extends Module {

    private final DoubleSetting range      = register(new DoubleSetting("Range",        "Attack range",                       3.5, 1.0, 6.0));
    private final BoolSetting   autoSwitch = register(new BoolSetting  ("Auto Switch",  "Auto-switch to the best axe",         true));
    private final BoolSetting   restore    = register(new BoolSetting  ("Restore Slot", "Restore previous slot after attack",  true));

    private int prevSlot = -1;

    public AxeOnly() {
        super("AxeOnly", "Attacks only while holding an axe; optionally auto-switches", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean holdingAxe = mc.player.getMainHandStack().getItem() instanceof AxeItem;

        if (!holdingAxe && autoSwitch.isEnabled()) {
            int axeSlot = InventoryUtil.findBestAxe();
            if (axeSlot == -1 || axeSlot > 8) return; // not in hotbar
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = axeSlot;
            holdingAxe = true;
        }

        if (!holdingAxe) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isRemoved()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (restore.isEnabled()) restoreSlot();
            return;
        }

        // No target — restore immediately so we don't stay on axe forever
        if (restore.isEnabled()) restoreSlot();
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        // Cancel attack if not holding axe and autoSwitch is off
        if (!autoSwitch.isEnabled() && !(mc.player.getMainHandStack().getItem() instanceof AxeItem)) {
            event.cancel();
        }
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
