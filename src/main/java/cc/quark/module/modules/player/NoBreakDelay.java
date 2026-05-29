package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

import java.lang.reflect.Field;

public class NoBreakDelay extends Module {

    private final BoolSetting alsoBlockDelay = register(new BoolSetting(
            "Block Place Delay", "Also remove block placement cooldown", false));

    private Field blockBreakingCooldownField = null;
    private Field blockPlaceCooldownField = null;

    public NoBreakDelay() {
        super("NoBreakDelay", "Removes the block-breaking cooldown for faster sequential mining", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Zero blockBreakingCooldown on interactionManager
        try {
            if (blockBreakingCooldownField == null) {
                blockBreakingCooldownField = findField(mc.interactionManager.getClass(),
                        "blockBreakingCooldown", "field_2692", "blockBreakingDelay");
            }
            if (blockBreakingCooldownField != null) {
                blockBreakingCooldownField.setAccessible(true);
                blockBreakingCooldownField.set(mc.interactionManager, 0);
            }
        } catch (Exception ignored) {}

        // Zero itemUseCooldown on player (block place delay)
        if (alsoBlockDelay.isEnabled()) {
            try {
                if (blockPlaceCooldownField == null) {
                    blockPlaceCooldownField = findField(mc.player.getClass(),
                            "itemUseTimeLeft", "field_7483");
                }
                if (blockPlaceCooldownField != null) {
                    blockPlaceCooldownField.setAccessible(true);
                    blockPlaceCooldownField.set(mc.player, 0);
                }
            } catch (Exception ignored) {}
        }
    }

    private static Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            Class<?> c = clazz;
            while (c != null) {
                try { return c.getDeclaredField(name); } catch (NoSuchFieldException e) {}
                c = c.getSuperclass();
            }
        }
        return null;
    }
}
