package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;

/**
 * AutoEquip - Automatically equips the best weapon or tool from the player's
 * inventory based on the configured mode and current situation.
 */
public class AutoEquip extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "What to auto-equip",
            "Weapon", "Weapon", "Pickaxe", "Axe", "Sword", "Shovel"));
    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Restore previous slot when condition clears", true));
    private final BoolSetting onlyNear = register(new BoolSetting(
            "Only Near Enemy", "Weapon mode: only switch when an enemy is within 8 blocks", true));

    private int prevSlot = -1;
    private boolean active = false;
    private final TimerUtil timer = new TimerUtil();

    public AutoEquip() {
        super("AutoEquip", "Automatically equips the best weapon or tool from inventory", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
        active = false;
    }

    @Override
    public void onDisable() {
        if (switchBack.isEnabled() && prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
        prevSlot = -1;
        active = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        boolean shouldEquip = shouldEquipNow();

        if (!shouldEquip) {
            if (active && switchBack.isEnabled() && prevSlot >= 0) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            active = false;
            return;
        }

        int best = findBestSlot();
        if (best < 0 || best >= 9) return;

        if (!active) {
            prevSlot = mc.player.getInventory().selectedSlot;
            active = true;
        }
        mc.player.getInventory().selectedSlot = best;
    }

    private boolean shouldEquipNow() {
        if (mode.is("Weapon") || mode.is("Sword")) {
            if (!onlyNear.isEnabled()) return true;
            for (var entity : mc.world.getEntities()) {
                if (entity == mc.player || !(entity instanceof LivingEntity le) || le.isRemoved()) continue;
                if (mc.player.distanceTo(le) <= 8.0) return true;
            }
            return false;
        }
        // For tools, always keep the best equipped
        return true;
    }

    private int findBestSlot() {
        return switch (mode.get()) {
            case "Sword"   -> InventoryUtil.findBestSword();
            case "Pickaxe" -> clampHotbar(InventoryUtil.findBestPickaxe());
            case "Axe"     -> clampHotbar(InventoryUtil.findBestAxe());
            case "Shovel"  -> findBestShovelHotbar();
            case "Weapon"  -> findBestWeapon();
            default        -> findBestWeapon();
        };
    }

    /** Returns the slot only if it is in the hotbar (0-8), else -1. */
    private int clampHotbar(int slot) {
        return (slot >= 0 && slot < 9) ? slot : -1;
    }

    private int findBestShovelHotbar() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof ShovelItem) return i;
        }
        return -1;
    }

    private int findBestWeapon() {
        if (mc.player == null) return -1;
        int best = -1;
        float bestDmg = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            Item item = stack.getItem();
            float dmg = 0;
            if (item instanceof SwordItem s) {
                dmg = s.getMaterial().value().attackDamageBonus() + 3f; // swords get base +3
            } else if (item instanceof AxeItem a) {
                dmg = a.getMaterial().value().attackDamageBonus() + 2f;
            }
            if (dmg > bestDmg) { bestDmg = dmg; best = i; }
        }
        return best;
    }
}
