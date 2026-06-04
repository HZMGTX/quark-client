package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RadarPlus extends Module {

    private final IntSetting  size      = register(new IntSetting ("Size",      "Radar size in pixels",   80, 40, 200));
    private final IntSetting  range     = register(new IntSetting ("Range",     "Radar detection range",  64, 16, 256));
    private final IntSetting  x         = register(new IntSetting ("X",         "Radar X position",       10, 0, 3840));
    private final IntSetting  y         = register(new IntSetting ("Y",         "Radar Y position",       10, 0, 2160));
    private final BoolSetting showNames = register(new BoolSetting("Names",     "Show player names",      false));
    private final ColorSetting bgColor  = register(new ColorSetting("BG Color", "Background color",       0x88000000));
    private final ColorSetting dotColor = register(new ColorSetting("Dot Color","Player dot color",       0xFFFF5555));

    public RadarPlus() {
        super("RadarPlus", "Enhanced player radar with names and configurable range", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        int rx = x.get(), ry = y.get(), sz = size.get();
        int half = sz / 2;

        ctx.fill(rx, ry, rx + sz, ry + sz, bgColor.get());
        ctx.fill(rx + half - 1, ry, rx + half + 1, ry + sz, 0x44FFFFFF);
        ctx.fill(rx, ry + half - 1, rx + sz, ry + half + 1, 0x44FFFFFF);

        Vec3d playerPos = mc.player.getPos();
        float playerYaw = mc.player.getYaw();
        float scale = half / (float) range.get();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p) || p == mc.player) continue;
            double dx = p.getX() - playerPos.x;
            double dz = p.getZ() - playerPos.z;
            if (Math.sqrt(dx * dx + dz * dz) > range.get()) continue;

            double rad = Math.toRadians(playerYaw);
            int rdx = (int)( dx * Math.cos(rad) + dz * Math.sin(rad));
            int rdz = (int)(-dx * Math.sin(rad) + dz * Math.cos(rad));

            int dotX = rx + half + (int)(rdx * scale);
            int dotY = ry + half + (int)(rdz * scale);
            dotX = Math.max(rx + 1, Math.min(rx + sz - 2, dotX));
            dotY = Math.max(ry + 1, Math.min(ry + sz - 2, dotY));

            ctx.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, dotColor.get());
            if (showNames.isEnabled()) {
                ctx.drawTextWithShadow(mc.textRenderer, p.getName().getString(), dotX + 3, dotY - 4, dotColor.get());
            }
        }

        ctx.drawText(mc.textRenderer, "N", rx + half - 2, ry + 1, 0xFFFFFFFF, false);
    }
}
