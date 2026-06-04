package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class KillTracker extends Module {

    private final BoolSetting showHUD = register(new BoolSetting("Show HUD", "Display kill count on screen", true));

    private int kills = 0;
    private final Map<Integer, Float> lastHealthMap = new HashMap<>();

    public KillTracker() {
        super("KillTracker", "Tracks kill count per session", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        kills = 0;
        lastHealthMap.clear();
    }

    @Override
    public void onDisable() {
        kills = 0;
        lastHealthMap.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;

            int id = entity.getId();
            float prev = lastHealthMap.getOrDefault(id, living.getHealth());

            if (prev > 0f && living.getHealth() <= 0f) {
                kills++;
            }

            lastHealthMap.put(id, living.getHealth());
        }

        // Clean up removed entities
        lastHealthMap.entrySet().removeIf(e ->
            mc.world.getEntityById(e.getKey()) == null
        );
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, "Kills: " + kills, 4, 14, 0xFFFFAA00);
    }

    public int getKills() {
        return kills;
    }
}
