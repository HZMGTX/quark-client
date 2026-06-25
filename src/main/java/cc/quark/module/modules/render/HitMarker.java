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
 * HitMarker - Draws a crosshair-style hit marker whenever the player deals damage.
 *
 * Health changes on nearby living entities are monitored each tick; if any entity
 * loses health the hit-marker is shown for {@code displayMs} milliseconds.
 */
public class HitMarker extends Module {

    private final IntSetting  size      = register(new IntSetting ("Size",       "Length of each hit-marker line in pixels", 6, 2, 20));
    private final IntSetting  gap       = register(new IntSetting ("Gap",        "Gap between lines and screen center",       3, 0, 10));
    private final IntSetting  displayMs = register(new IntSetting ("Display Ms", "How long to show the marker (ms)",         200, 50, 1000));
    private final ColorSetting color    = register(new ColorSetting("Color",     "Hit marker color (ARGB)",                  0xFFFF4444));

    private long lastHitTime = 0;
    private final Map<Integer, Float> prevHealth = new HashMap<>();

    public HitMarker() {
        super("HitMarker", "Shows crosshair hit marker when dealing damage", Category.RENDER);
    }

    @Override
    public void onEnable() {
        prevHealth.clear();
        lastHitTime = 0;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;

            float prev = prevHealth.getOrDefault(entity.getId(), living.getHealth());
            if (living.getHealth() < prev) {
                lastHitTime = System.currentTimeMillis();
            }
            prevHealth.put(entity.getId(), living.getHealth());
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.getWindow() == null) return;
        if (System.currentTimeMillis() - lastHitTime > displayMs.get()) return;

        DrawContext ctx  = e.getDrawContext();
        int sw   = mc.getWindow().getScaledWidth();
        int sh   = mc.getWindow().getScaledHeight();
        int cx   = sw / 2;
        int cy   = sh / 2;
        int len  = size.get();
        int g    = gap.get();
        int col  = color.get();

        // Top line
        ctx.fill(cx - 1, cy - g - len, cx + 1, cy - g, col);
        // Bottom line
        ctx.fill(cx - 1, cy + g,       cx + 1, cy + g + len, col);
        // Left line
        ctx.fill(cx - g - len, cy - 1, cx - g, cy + 1, col);
        // Right line
        ctx.fill(cx + g,       cy - 1, cx + g + len, cy + 1, col);
    }
}
