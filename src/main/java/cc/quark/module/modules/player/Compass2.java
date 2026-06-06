package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

/**
 * Compass2 — shows a real-time compass direction as a HUD overlay.
 * Displays cardinal direction (N/S/E/W) and yaw angle on screen.
 */
public class Compass2 extends Module {

    private final IntSetting x = register(new IntSetting(
            "X", "HUD X position", 4, 0, 1920));
    private final IntSetting y = register(new IntSetting(
            "Y", "HUD Y position", 4, 0, 1080));
    private final BoolSetting showCoords = register(new BoolSetting(
            "Show Coords", "Show XYZ coordinates below compass", true));
    private final BoolSetting showAngle = register(new BoolSetting(
            "Show Angle", "Show raw yaw angle", false));

    private String direction = "N";
    private float yaw = 0f;
    private double px = 0, py = 0, pz = 0;

    public Compass2() {
        super("Compass2", "Shows real-time compass direction as HUD overlay", Category.PLAYER);
    }

    private String yawToDirection(float yaw) {
        // Normalize yaw to [0, 360)
        float normalized = ((yaw % 360f) + 360f) % 360f;
        if (normalized < 22.5f || normalized >= 337.5f) return "S";
        if (normalized < 67.5f)  return "SW";
        if (normalized < 112.5f) return "W";
        if (normalized < 157.5f) return "NW";
        if (normalized < 202.5f) return "N";
        if (normalized < 247.5f) return "NE";
        if (normalized < 292.5f) return "E";
        return "SE";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        yaw = mc.player.getYaw();
        direction = yawToDirection(yaw);
        px = mc.player.getX();
        py = mc.player.getY();
        pz = mc.player.getZ();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        var ctx = event.getDrawContext();
        var font = mc.textRenderer;

        int posX = x.get();
        int posY = y.get();
        int color = 0xFFFFFF;
        int shadow = 0x000000;

        String compassText = "[ " + direction + " ]";
        ctx.drawTextWithShadow(font, compassText, posX, posY, color);

        if (showAngle.isEnabled()) {
            float normalizedYaw = ((yaw % 360f) + 360f) % 360f;
            String angleText = String.format("%.1f°", normalizedYaw);
            ctx.drawTextWithShadow(font, angleText, posX, posY + 10, 0xAAAAAA);
        }

        if (showCoords.isEnabled()) {
            String coordText = String.format("X:%.0f Y:%.0f Z:%.0f", px, py, pz);
            ctx.drawTextWithShadow(font, coordText, posX, posY + 20, 0xAAAAAA);
        }
    }
}
