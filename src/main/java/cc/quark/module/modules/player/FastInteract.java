package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

import java.lang.reflect.Field;

public class FastInteract extends Module {

    private final BoolSetting removeDelay = register(new BoolSetting(
            "Remove Delay", "Zero out the item use cooldown via reflection", true));
    private final IntSetting usePerTick = register(new IntSetting(
            "Uses Per Tick", "Extra interact calls per tick", 1, 1, 5));

    private Field itemUseCooldownField = null;

    public FastInteract() {
        super("FastInteract", "Removes interaction cooldown for faster block/item use", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Clear item use cooldown via reflection
        if (removeDelay.isEnabled()) {
            try {
                if (itemUseCooldownField == null) {
                    for (Field f : mc.player.getClass().getSuperclass().getDeclaredFields()) {
                        if (f.getType() == int.class && f.getName().contains("itemUse")) {
                            itemUseCooldownField = f;
                            break;
                        }
                    }
                    if (itemUseCooldownField == null) {
                        // Try by common obfuscated name
                        itemUseCooldownField = findField(mc.player.getClass(), "itemUseTimeLeft", "field_7483");
                    }
                }
                if (itemUseCooldownField != null) {
                    itemUseCooldownField.setAccessible(true);
                    itemUseCooldownField.set(mc.player, 0);
                }
            } catch (Exception ignored) {}
        }

        // Extra interact calls when use key held and targeting a block
        if (mc.crosshairTarget != null
                && mc.crosshairTarget.getType() == HitResult.Type.BLOCK
                && mc.options.useKey.isPressed()) {
            for (int i = 0; i < usePerTick.get(); i++) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
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
