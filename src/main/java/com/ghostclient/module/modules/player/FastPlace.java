package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.IntSetting;
import net.minecraft.client.network.ClientPlayerInteractionManager;

import java.lang.reflect.Field;

/**
 * FastPlace - removes the right-click delay between block placements.
 *
 * Vanilla Minecraft enforces a 4-tick cooldown between uses of items/blocks
 * (itemUseCooldown in ClientPlayerInteractionManager). We reset this to 0
 * every tick so blocks can be placed as fast as possible.
 */
public class FastPlace extends Module {

    public static FastPlace INSTANCE;

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between placements (0 = no delay)", 0, 0, 5));

    // Cached reflection field for itemUseCooldown
    private static Field itemUseCooldownField;

    static {
        try {
            // Field is named differently in obfuscated vs. deobfuscated;
            // use a known mapped name from Yarn 1.20.4 mappings.
            itemUseCooldownField = ClientPlayerInteractionManager.class.getDeclaredField("itemUseCooldown");
            itemUseCooldownField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // Try alternate name used in some mappings
            try {
                itemUseCooldownField = ClientPlayerInteractionManager.class.getDeclaredField("field_3684");
                itemUseCooldownField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                System.err.println("[FastPlace] Could not find itemUseCooldown field: " + ex.getMessage());
            }
        }
    }

    public FastPlace() {
        super("FastPlace", "Reduces right-click placement delay to zero", Category.PLAYER);
        INSTANCE = this;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.interactionManager == null) return;
        if (itemUseCooldownField == null) return;

        try {
            int current = (int) itemUseCooldownField.get(mc.interactionManager);
            if (current > delay.get()) {
                itemUseCooldownField.set(mc.interactionManager, delay.get());
            }
        } catch (IllegalAccessException e) {
            // Ignore
        }
    }

    /**
     * Returns current configured delay. Used by MixinPlayerInteractHandler.
     */
    public int getDelay() {
        return delay.get();
    }
}
