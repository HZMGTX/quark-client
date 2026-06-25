package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

public class AntiEnchant extends Module {

    private final BoolSetting switchWeapon = register(new BoolSetting("Switch Weapon", "Switch to axe vs high-protection targets", true));
    private final BoolSetting avoidSharp = register(new BoolSetting("Avoid Sharpness", "Stay at range when target has sharpness sword", true));
    private final DoubleSetting retreatRange = register(new DoubleSetting("Retreat Range", "Distance to maintain vs sharpness targets", 3.5, 2.0, 6.0));

    private boolean targetHasSharpness = false;
    private boolean targetHasProtection = false;
    private int axeSlot = -1;
    private int swordSlot = -1;

    public AntiEnchant() {
        super("AntiEnchant", "Adapts attack strategy based on enemy enchantments", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        targetHasSharpness = false;
        targetHasProtection = false;
        axeSlot = -1;
        swordSlot = -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && axeSlot != -1 && swordSlot != -1) {
            mc.player.getInventory().selectedSlot = swordSlot;
        }
        targetHasSharpness = false;
        targetHasProtection = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Find nearest player target
        PlayerEntity target = null;
        double best = 10.0;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) {
                best = d;
                target = p;
            }
        }

        if (target == null) {
            targetHasSharpness = false;
            targetHasProtection = false;
            return;
        }

        // Analyse target weapon enchants
        ItemStack weapon = target.getMainHandStack();
        targetHasSharpness = hasEnchant(weapon, "sharpness") || hasEnchant(weapon, "smite") || hasEnchant(weapon, "bane_of_arthropods");

        // Analyse target armor enchants (check all 4 armor slots)
        int totalProt = 0;
        for (int slot = 36; slot <= 39; slot++) {
            ItemStack armor = target.getInventory().getStack(slot);
            totalProt += getEnchantLevel(armor, "protection")
                    + getEnchantLevel(armor, "blast_protection")
                    + getEnchantLevel(armor, "projectile_protection");
        }
        targetHasProtection = totalProt >= 12;

        // Find axe and sword in hotbar
        axeSlot = -1;
        swordSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (s.getItem() instanceof AxeItem && axeSlot == -1) axeSlot = i;
            if (s.getItem() instanceof SwordItem && swordSlot == -1) swordSlot = i;
        }

        // Switch to axe vs heavily-protected targets (axe ignores shield/armor better)
        if (switchWeapon.isEnabled() && targetHasProtection && axeSlot != -1) {
            mc.player.getInventory().selectedSlot = axeSlot;
        } else if (switchWeapon.isEnabled() && !targetHasProtection && swordSlot != -1) {
            mc.player.getInventory().selectedSlot = swordSlot;
        }

        // Back up from sharpness targets
        if (avoidSharp.isEnabled() && targetHasSharpness && best < retreatRange.get()) {
            double dx = mc.player.getX() - target.getX();
            double dz = mc.player.getZ() - target.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.001) {
                double speed = 0.25;
                mc.player.setVelocity(
                        mc.player.getVelocity().x + (dx / len) * speed,
                        mc.player.getVelocity().y,
                        mc.player.getVelocity().z + (dz / len) * speed
                );
            }
        }
    }

    private boolean hasEnchant(ItemStack stack, String id) {
        if (stack.isEmpty()) return false;
        ItemEnchantmentsComponent enc = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enc == null) return false;
        for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enc.getEnchantmentEntries()) {
            if (entry.getKey().getIdAsString().contains(id)) return true;
        }
        return false;
    }

    private int getEnchantLevel(ItemStack stack, String id) {
        if (stack.isEmpty()) return 0;
        ItemEnchantmentsComponent enc = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enc == null) return 0;
        for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enc.getEnchantmentEntries()) {
            if (entry.getKey().getIdAsString().contains(id)) return entry.getValue();
        }
        return 0;
    }

    @Override
    public String getSuffix() {
        if (targetHasSharpness && targetHasProtection) return "Sword+Prot";
        if (targetHasSharpness) return "Sharpness";
        if (targetHasProtection) return "Protected";
        return "Normal";
    }
}
