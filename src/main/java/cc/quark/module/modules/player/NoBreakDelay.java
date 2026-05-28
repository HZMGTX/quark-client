package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

import java.lang.reflect.Field;

public class NoBreakDelay extends Module {

    private Field blockBreakingCooldownField = null;

    public NoBreakDelay() {
        super("NoBreakDelay", "Removes the block-break cooldown", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        try {
            if (blockBreakingCooldownField == null) {
                blockBreakingCooldownField = findField(mc.interactionManager.getClass(), "blockBreakingCooldown", "field_2692");
            }
            if (blockBreakingCooldownField != null) {
                blockBreakingCooldownField.setAccessible(true);
                blockBreakingCooldownField.set(mc.interactionManager, 0);
            }
        } catch (Exception ignored) {}
    }

    private static Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            Class<?> c = clazz;
            while (c != null) {
                try {
                    return c.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
        }
        return null;
    }
}
