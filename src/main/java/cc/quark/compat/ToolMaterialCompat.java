package cc.quark.compat;

import net.minecraft.item.AxeItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;

/**
 * Handles ToolMaterial API changes across Minecraft versions.
 *
 * <p>In 1.21.1+, {@code getMaterial()} returns a {@code RegistryEntry<ToolMaterial>},
 * so fields must be accessed via {@code .value().attackDamageBonus()} / {@code .value().speed()}.
 *
 * <p>In 1.20.x and earlier the material is a plain {@code ToolMaterial} interface with
 * {@code getAttackDamage()} / {@code getMiningSpeedMultiplier()} methods.
 *
 * <p>Use these wrappers so the call sites remain identical across all supported versions.
 * When adding a new version that breaks these methods, update only this class.
 */
public final class ToolMaterialCompat {

    private ToolMaterialCompat() {}

    /** Attack damage bonus for the given sword's material. */
    public static float getAttackDamage(SwordItem sword) {
        return sword.getMaterial().value().attackDamageBonus();
    }

    /** Mining speed multiplier for the given pickaxe's material. */
    public static float getMiningSpeed(PickaxeItem pick) {
        return pick.getMaterial().value().speed();
    }

    /** Attack damage bonus for the given axe's material. */
    public static float getAxeDamage(AxeItem axe) {
        return axe.getMaterial().value().attackDamageBonus();
    }
}
