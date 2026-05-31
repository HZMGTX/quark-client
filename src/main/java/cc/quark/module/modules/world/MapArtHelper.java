package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class MapArtHelper extends Module {

    private final IntSetting gridSize = register(new IntSetting("GridSize", "Overlay grid cell size in pixels", 8, 4, 32));
    private final BoolSetting snapGuide = register(new BoolSetting("SnapGuide", "Show a crosshair guide snapped to map grid", true));

    public MapArtHelper() {
        super("MapArtHelper", "Shows a map image overlay and snaps block placement to pixel art design", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        var context = event.getDrawContext();

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int cell = gridSize.get();

        int color = 0x44FFFFFF;
        for (int x = 0; x < screenW; x += cell) {
            context.fill(x, 0, x + 1, screenH, color);
        }
        for (int y = 0; y < screenH; y += cell) {
            context.fill(0, y, screenW, y + 1, color);
        }

        if (snapGuide.isEnabled()) {
            int cx = screenW / 2;
            int cy = screenH / 2;
            int snappedX = (cx / cell) * cell + cell / 2;
            int snappedY = (cy / cell) * cell + cell / 2;
            context.fill(snappedX - 4, snappedY, snappedX + 5, snappedY + 1, 0xFFFF4444);
            context.fill(snappedX, snappedY - 4, snappedX + 1, snappedY + 5, 0xFFFF4444);
        }
    }
}
