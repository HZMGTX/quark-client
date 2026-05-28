package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.player.ItemCooldownManager;

import java.lang.reflect.Field;

/**
 * NoCooldown - removes item use and attack cooldowns via reflection.
 *
 * Attack:  resets the player's attack cooldown each tick.
 * Bow:     keeps bow charge progress at maximum so it fires immediately.
 * Pearl:   clears the ender pearl cooldown in the ItemCooldownManager.
 */
public class NoCooldown extends Module {

    private final BoolSetting attack = register(new BoolSetting(
            "Attack", "Remove attack cooldown", true));

    private final BoolSetting bow = register(new BoolSetting(
            "Bow", "Remove bow charge time", true));

    private final BoolSetting pearl = register(new BoolSetting(
            "Pearl", "Remove ender pearl cooldown", true));

    // Cached reflection fields
    private Field lastAttackedTicks = null;
    private Field itemUseCooldown   = null;

    public NoCooldown() {
        super("NoCooldown", "Removes attack, bow, and item cooldowns", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        // Cache fields once
        try {
            // net.minecraft.entity.LivingEntity or ClientPlayerEntity attack ticks
            lastAttackedTicks = mc.player != null
                    ? findField(mc.player.getClass(), "lastAttackedTicks", "field_6010")
                    : null;
        } catch (Exception ignored) {}

        try {
            // itemUseCooldown in ItemCooldownManager is a private map; access via clear()
            itemUseCooldown = null;
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable() {
        lastAttackedTicks = null;
        itemUseCooldown   = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Attack cooldown: set attackCooldown to 0 so full-damage swings are always ready
        if (attack.isEnabled()) {
            try {
                mc.player.getAttackCooldownProgress(0f); // ensure field accessed
                setField(mc.player, "lastAttackedTicks", "field_6010", 0);
                // Also reset via the player's handler
                mc.player.resetLastAttackedTicks();
            } catch (Exception ignored) {}
        }

        // Bow: keep item use ticks maxed out so arrow fires at full power immediately
        if (bow.isEnabled() && mc.player.isUsingItem()) {
            try {
                // Force getItemUseTimeLeft to 0 by setting itemUseTimeLeft field
                setField(mc.player, "itemUseTimeLeft", "field_7483", 0);
            } catch (Exception ignored) {}
        }

        // Pearl: clear all cooldowns in ItemCooldownManager
        if (pearl.isEnabled()) {
            try {
                ItemCooldownManager cooldownManager = mc.player.getItemCooldownManager();
                // Clear via reflection – CooldownManager.cooldowns is a Map
                Field cooldownsField = findField(cooldownManager.getClass(), "cooldowns", "field_9330");
                if (cooldownsField != null) {
                    cooldownsField.setAccessible(true);
                    ((java.util.Map<?, ?>)cooldownsField.get(cooldownManager)).clear();
                }
            } catch (Exception ignored) {}
        }
    }

    /** Find a field by one of several possible names (obfuscated or mapped). */
    private static Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            Class<?> c = clazz;
            while (c != null) {
                try {
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static void setField(Object target, String name1, String name2, int value) {
        Field f = findField(target.getClass(), name1, name2);
        if (f != null) {
            try { f.set(target, value); } catch (Exception ignored) {}
        }
    }
}
