package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * HitColor - Briefly flashes a colored screen overlay when the player lands a hit.
 */
public class HitColor extends Module {

    private final ColorSetting flashColor = register(new ColorSetting(
            "FlashColor", "Screen flash tint color", 0x55FF0000));
    private final IntSetting flashMs = register(new IntSetting(
            "FlashMs", "Duration of the flash in milliseconds", 100, 20, 500));

    private long lastFlashMs = 0;
    private final Map<Integer, Float> prevHealth = new HashMap<>();

    public HitColor() {
        super("HitColor", "Changes screen color tint when hitting", Category.RENDER);
    }

    @Override
    public void onEnable() {
        prevHealth.clear();
        lastFlashMs = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            float prev = prevHealth.getOrDefault(entity.getId(), living.getHealth());
            if (living.getHealth() < prev) {
                lastFlashMs = System.currentTimeMillis();
            }
            prevHealth.put(entity.getId(), living.getHealth());
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (System.currentTimeMillis() - lastFlashMs > flashMs.get()) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        ctx.fill(0, 0, sw, sh, flashColor.get());
    }
}
