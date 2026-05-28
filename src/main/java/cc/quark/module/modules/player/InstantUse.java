package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.Hand;

import java.lang.reflect.Field;

/**
 * InstantUse - instantly uses items and/or places blocks by resetting cooldown
 * fields via reflection.
 *
 * Items:  resets the item use cooldown so consumables finish immediately.
 * Blocks: resets the right-click delay so blocks can be placed every tick.
 */
public class InstantUse extends Module {

    private final BoolSetting items = register(new BoolSetting(
            "Items", "Instantly use items (resets itemUseCooldown)", true));

    private final BoolSetting blocks = register(new BoolSetting(
            "Blocks", "Instantly place blocks (resets rightClickDelay)", true));

    public InstantUse() {
        super("InstantUse", "Completes item use and block placement instantly", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Items: clear all item cooldowns so items can be used every tick
        if (items.isEnabled()) {
            try {
                ItemCooldownManager mgr = mc.player.getItemCooldownManager();
                Field f = findField(mgr.getClass(), "cooldowns", "field_9330");
                if (f != null) {
                    f.setAccessible(true);
                    ((java.util.Map<?, ?>) f.get(mgr)).clear();
                }
            } catch (Exception ignored) {}

            // Also finish active item use immediately by spamming interactItem
            if (mc.player.isUsingItem() && mc.player.getItemUseTimeLeft() > 0) {
                for (int i = 0; i < 20; i++) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                }
            }
        }

        // Blocks: reset the interaction manager's right-click delay
        if (blocks.isEnabled()) {
            try {
                setField(mc.interactionManager, "blockBreakingCooldown", "field_2692", 0);
            } catch (Exception ignored) {}
            // Also try "rightClickDelay" naming used in some mappings
            try {
                setField(mc.interactionManager, "rightClickDelay", "field_2692", 0);
            } catch (Exception ignored) {}
        }
    }

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
