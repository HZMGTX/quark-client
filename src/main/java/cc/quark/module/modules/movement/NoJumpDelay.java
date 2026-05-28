package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

import java.lang.reflect.Field;

public class NoJumpDelay extends Module {

    private Field jumpCooldownField = null;

    public NoJumpDelay() {
        super("NoJumpDelay", "Removes jump cooldown", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        jumpCooldownField = null;
        if (mc.player == null) return;
        Class<?> cls = mc.player.getClass();
        while (cls != null && jumpCooldownField == null) {
            for (Field f : cls.getDeclaredFields()) {
                if (f.getType() != int.class) continue;
                String lower = f.getName().toLowerCase();
                if (lower.contains("jump") && lower.contains("cool")) {
                    f.setAccessible(true);
                    jumpCooldownField = f;
                    break;
                }
            }
            cls = cls.getSuperclass();
        }
        if (jumpCooldownField == null) {
            cls = mc.player.getClass();
            while (cls != null && jumpCooldownField == null) {
                for (Field f : cls.getDeclaredFields()) {
                    if (f.getType() != int.class) continue;
                    String lower = f.getName().toLowerCase();
                    if (lower.contains("jump")) {
                        f.setAccessible(true);
                        jumpCooldownField = f;
                        break;
                    }
                }
                cls = cls.getSuperclass();
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (jumpCooldownField == null) {
            onEnable();
            return;
        }
        try {
            jumpCooldownField.set(mc.player, 0);
        } catch (Exception ignored) {}
    }
}
