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
            "Delay", "Ticks between placements (0 = no delay)", 0, 0, 5));

    private final BoolSetting onHold = register(new BoolSetting(
            "On Hold", "Only reduce delay while right-click is held", true));

    private static Field itemUseCooldownField;

    static {
        try {
            itemUseCooldownField = ClientPlayerInteractionManager.class.getDeclaredField("itemUseCooldown");
            itemUseCooldownField.setAccessible(true);
        } catch (NoSuchFieldException e) {
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

        if (onHold.isEnabled() && !mc.options.useKey.isPressed()) return;

        try {
            int current = (int) itemUseCooldownField.get(mc.interactionManager);
            if (current > delay.get()) {
                itemUseCooldownField.set(mc.interactionManager, delay.get());
            }
        } catch (IllegalAccessException e) {
            // Ignore
        }
    }

    public int getDelay() {
        return delay.get();
    }
}
