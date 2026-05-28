package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.player.ItemCooldownManager;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * NoCooldown - removes item use and attack cooldowns via reflection.
 *
 * Attack: resets the player's attack cooldown each tick (via reflection field).
 * Bow:    forces item use time to 0 so bows fire instantly.
 * Pearl:  clears the ItemCooldownManager map each tick.
 */
public class NoCooldown extends Module {

    private final BoolSetting attack = register(new BoolSetting(
            "Attack", "Remove attack cooldown", true));

    private final BoolSetting bow = register(new BoolSetting(
            "Bow", "Remove bow charge time", true));

    private final BoolSetting pearl = register(new BoolSetting(
            "Pearl", "Remove ender pearl cooldown", true));

    public NoCooldown() {
        super("NoCooldown", "Removes attack, bow, and item cooldowns", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Attack cooldown: zero out the last-attacked-ticks field via reflection
        if (attack.isEnabled()) {
            setFieldByName(mc.player, 0,
                    "lastAttackedTicks",  // Yarn mapped
                    "field_6010"          // Intermediary
            );
        }

        // Bow: force the item use time left to 0 so arrow fires at full power immediately
        if (bow.isEnabled() && mc.player.isUsingItem()) {
            setFieldByName(mc.player, 0,
                    "itemUseTimeLeft",    // Yarn mapped
                    "field_7483"          // Intermediary
            );
        }

        // Pearl / item cooldowns: clear all cooldown entries in the manager
        if (pearl.isEnabled()) {
            try {
                ItemCooldownManager mgr = mc.player.getItemCooldownManager();
                Field f = findField(mgr.getClass(), "cooldowns", "field_9330");
                if (f != null) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> map = (Map<Object, Object>) f.get(mgr);
                    if (map != null) map.clear();
                }
            } catch (Exception ignored) {}
        }
    }

    // -------------------------------------------------------------------------
    // Reflection helpers
    // -------------------------------------------------------------------------

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

    private static void setFieldByName(Object target, int value, String... names) {
        Field f = findField(target.getClass(), names);
        if (f != null) {
            try { f.set(target, value); } catch (Exception ignored) {}
        }
    }
}
