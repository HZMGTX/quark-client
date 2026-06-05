package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;

public class AutoSwap extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Swap trigger condition",
            "CombatRange", "CombatRange", "LowHP", "Always"));
    private final DoubleSetting combatRange = register(new DoubleSetting(
            "Combat Range", "Swap to weapon when enemy within this range", 5.0, 2.0, 10.0));
    private final DoubleSetting hpThreshold = register(new DoubleSetting(
            "HP Threshold", "HP to swap in LowHP mode (half-hearts)", 8.0, 1.0, 18.0));
    private final BoolSetting preferAxe = register(new BoolSetting(
            "Prefer Axe", "Use best axe instead of sword when available", false));
    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Restore previous slot when leaving combat", true));

    private int savedSlot = -1;
    private boolean inCombat = false;

    public AutoSwap() {
        super("AutoSwap", "Equips best weapon when enemies are nearby or HP is low", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        savedSlot = -1;
        inCombat = false;
    }

    @Override
    public void onDisable() {
        if (restoreSlot.isEnabled() && savedSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
        }
        savedSlot = -1;
        inCombat = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean trigger = shouldTrigger();

        if (trigger && !inCombat) {
            inCombat = true;
            savedSlot = mc.player.getInventory().selectedSlot;
            switchToWeapon();
        } else if (!trigger && inCombat) {
            inCombat = false;
            if (restoreSlot.isEnabled() && savedSlot >= 0) {
                mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
        }
    }

    private boolean shouldTrigger() {
        return switch (mode.get()) {
            case "CombatRange" -> hasEnemyInRange(combatRange.get());
            case "LowHP"       -> mc.player.getHealth() <= hpThreshold.get();
            case "Always"      -> true;
            default            -> false;
        };
    }

    private boolean hasEnemyInRange(double range) {
        for (var e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le) || le.isRemoved()) continue;
            if (mc.player.distanceTo(le) <= range) return true;
        }
        return false;
    }

    private void switchToWeapon() {
        int slot = preferAxe.isEnabled()
                ? InventoryUtil.findBestAxe()
                : InventoryUtil.findBestSword();
        if (slot < 0) slot = preferAxe.isEnabled()
                ? InventoryUtil.findBestSword()  // fallback
                : InventoryUtil.findBestAxe();
        if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }
}
