package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityList extends Module {

    private final DoubleSetting maxRange = register(new DoubleSetting("Range",   "Only count entities within this range", 64, 8, 256));
    private final IntSetting    posX     = register(new IntSetting("X",          "HUD X position",                        5, 0, 800));
    private final IntSetting    posY     = register(new IntSetting("Y",          "HUD Y position",                        100, 0, 500));
    private final IntSetting    maxLines = register(new IntSetting("Max Lines",  "Max entity types to show",               10, 3, 20));

    public EntityList() {
        super("EntityList", "Shows a list of nearby entity counts grouped by type", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;

        Map<String, Integer> counts = new LinkedHashMap<>();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (mc.player.distanceTo(entity) > maxRange.get()) continue;

            String name = entity instanceof PlayerEntity pe
                    ? pe.getName().getString()
                    : Registries.ENTITY_TYPE.getId(entity.getType()).getPath();

            counts.merge(name, 1, Integer::sum);
        }

        if (counts.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        int px = posX.get(), py = posY.get();
        int lines = 0;

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (++lines > maxLines.get()) break;
            String text = e.getKey() + " x" + e.getValue();
            ctx.drawTextWithShadow(mc.textRenderer, text, px, py, 0xFFFFFFFF);
            py += 10;
        }
    }
}
