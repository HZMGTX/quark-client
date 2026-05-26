package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

public class CombatInfo extends Module {

    public CombatInfo() {
        super("CombatInfo", "Shows nearest entity name, health, and distance on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (var e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e == mc.player) continue;
            double d = mc.player.distanceTo(le);
            if (d < best) { best = d; nearest = le; }
        }
        if (nearest == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        String name = nearest.getName().getString();
        String health = String.format("HP: %.1f", nearest.getHealth());
        String dist = String.format("Dist: %.1fm", best);

        int x = screenW - 120;
        ctx.fill(x - 2, 3, screenW - 2, 38, 0xAA181818);
        ctx.drawTextWithShadow(mc.textRenderer, name, x, 7, 0xFFFFFFFF);
        ctx.drawTextWithShadow(mc.textRenderer, health, x, 17, 0xFFFF5555);
        ctx.drawTextWithShadow(mc.textRenderer, dist, x, 27, 0xFFAAAAAA);
    }
}
