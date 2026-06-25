package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.math.Vec3d;

public class ExplosiveBolt extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max display range for bolts", 20.0, 5.0, 50.0));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Bolt trail color", 0xFFFF5500));

    public ExplosiveBolt() {
        super("ExplosiveBolt", "Crossbow bolts explode on impact (client visual)", Category.COMBAT);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int c = color.get();

        // Render a visual indicator for every tracked arrow/bolt near the player
        int count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ArrowEntity) && !(entity instanceof TridentEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            count++;
        }

        if (count > 0) {
            String text = "Bolts: " + count;
            // Draw a pulsing indicator
            long time = System.currentTimeMillis();
            int alpha = (int) (Math.abs(Math.sin(time / 400.0)) * 200 + 55);
            int displayColor = (alpha << 24) | (c & 0x00FFFFFF);
            ctx.drawText(mc.textRenderer, text, screenW / 2 - mc.textRenderer.getWidth(text) / 2,
                    screenH / 2 - 30, displayColor, true);
        }
    }
}
