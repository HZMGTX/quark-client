package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.network.ClientPlayerInteractionManager;

import java.lang.reflect.Field;

public class FastPlace extends Module {

    public static FastPlace INSTANCE;

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between placements (0 = no delay)", 0, 0, 4));

    private final BoolSetting fullDelay = register(new BoolSetting(
            "Full Delay", "Set item use cooldown to 0 ticks every tick", true));

    private final BoolSetting onHold = register(new BoolSetting(
            "On Hold", "Only reduce delay while right-click is held", true));

    private static Field itemUseCooldownField;
    private static Field attackCooldownField;

    static {
        // Try obfuscated and deobfuscated field names for itemUseCooldown
        for (String name : new String[]{"itemUseCooldown", "field_3684"}) {
            try {
                itemUseCooldownField = ClientPlayerInteractionManager.class.getDeclaredField(name);
                itemUseCooldownField.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {}
        }
        if (itemUseCooldownField == null) {
            System.err.println("[FastPlace] Could not find itemUseCooldown field.");
        }

        // Try to find attackCooldown field
        for (String name : new String[]{"attackCooldown", "field_3680"}) {
            try {
                attackCooldownField = ClientPlayerInteractionManager.class.getDeclaredField(name);
                attackCooldownField.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {}
        }
    }

    public FastPlace() {
        super("FastPlace", "Reduces right-click placement delay to zero", Category.PLAYER);
        INSTANCE = this;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.interactionManager == null) return;

        if (onHold.isEnabled() && !mc.options.useKey.isPressed()) return;

        // Handle itemUseCooldown
        if (itemUseCooldownField != null) {
            try {
                int target = fullDelay.isEnabled() ? 0 : delay.get();
                int current = (int) itemUseCooldownField.get(mc.interactionManager);
                if (current > target) {
                    itemUseCooldownField.set(mc.interactionManager, target);
                }
            } catch (IllegalAccessException e) {
                // Ignore
            }
        }

        // Handle attackCooldown if Full Delay is enabled
        if (fullDelay.isEnabled() && attackCooldownField != null) {
            try {
                int current = (int) attackCooldownField.get(mc.interactionManager);
                if (current > 0) {
                    attackCooldownField.set(mc.interactionManager, 0);
                }
            } catch (IllegalAccessException e) {
                // Ignore
            }
        }
    }

    public int getDelay() {
        return delay.get();
    }
}
