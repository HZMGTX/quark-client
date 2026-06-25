package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class MobCounter extends Module {

    private final IntSetting range       = register(new IntSetting("Range", "Detection radius in blocks", 64, 16, 256));
    private final BoolSetting showHostile = register(new BoolSetting("ShowHostile", "Count hostile mobs", true));
    private final BoolSetting showPassive = register(new BoolSetting("ShowPassive", "Count passive mobs", true));
    private final IntSetting posX        = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting posY        = register(new IntSetting("Y", "HUD Y position", 60, 0, 3000));

    public MobCounter() {
        super("MobCounter", "HUD display of nearby mob counts by type", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int r = range.get();
        Map<String, Integer> counts = new LinkedHashMap<>();
        int totalHostile = 0, totalPassive = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (mc.player.distanceTo(entity) > r) continue;
            if (showHostile.isEnabled() && entity instanceof HostileEntity) {
                String name = entity.getType().getName().getString();
                counts.merge(name, 1, Integer::sum);
                totalHostile++;
            } else if (showPassive.isEnabled() && entity instanceof AnimalEntity) {
                totalPassive++;
            }
        }

        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        if (showHostile.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Hostile: " + totalHostile, x, y, 0xFFFF5555);
            y += lh;
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                ctx.drawTextWithShadow(mc.textRenderer, "  " + e.getKey() + ": " + e.getValue(), x, y, 0xFFFFAAAA);
                y += lh;
            }
        }
        if (showPassive.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Passive: " + totalPassive, x, y, 0xFF55FF55);
        }
    }
}
