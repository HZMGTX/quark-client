package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class PlayerRadar extends Module {

    private final IntSetting posX   = register(new IntSetting("X",      "Radar center X on screen", 100, 0, 1000));
    private final IntSetting posY   = register(new IntSetting("Y",      "Radar center Y on screen",  50, 0, 1000));
    private final IntSetting range  = register(new IntSetting("Range",  "Block radius to display",   64, 16, 256));
    private final IntSetting size   = register(new IntSetting("Size",   "Radar widget half-size in pixels", 50, 20, 150));
    private final ColorSetting bgColor     = register(new ColorSetting("Background", "Radar background color",  0xAA000000));
    private final ColorSetting dotColor    = register(new ColorSetting("Dot Color",  "Player dot color",        0xFFFF4444));
    private final ColorSetting selfColor   = register(new ColorSetting("Self Color", "Self dot color",          0xFF44FF44));
    private final ColorSetting borderColor = register(new ColorSetting("Border",     "Border color",            0xFF888888));

    public PlayerRadar() {
        super("PlayerRadar", "Renders a 2D radar showing nearby player positions", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int cx = posX.get();
        int cy = posY.get();
        int half = size.get();
        int r = range.get();

        ctx.fill(cx - half, cy - half, cx + half, cy + half, bgColor.get());
        ctx.fill(cx - half, cy - half, cx + half, cy - half + 1, borderColor.get());
        ctx.fill(cx - half, cy + half - 1, cx + half, cy + half, borderColor.get());
        ctx.fill(cx - half, cy - half, cx - half + 1, cy + half, borderColor.get());
        ctx.fill(cx + half - 1, cy - half, cx + half, cy + half, borderColor.get());

        ctx.fill(cx - 1, cy - half, cx + 1, cy + half, 0x33FFFFFF);
        ctx.fill(cx - half, cy - 1, cx + half, cy + 1, 0x33FFFFFF);

        double selfX = mc.player.getX();
        double selfZ = mc.player.getZ();
        float yaw = mc.player.getYaw();

        List<PlayerEntity> players = mc.world.getPlayers();
        for (PlayerEntity p : players) {
            if (p == mc.player) continue;
            double dx = p.getX() - selfX;
            double dz = p.getZ() - selfZ;

            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > r) continue;

            double rad = Math.toRadians(-yaw);
            double rx = dx * Math.cos(rad) - dz * Math.sin(rad);
            double rz = dx * Math.sin(rad) + dz * Math.cos(rad);

            int px = cx + (int)(rx / r * half);
            int pz = cy + (int)(rz / r * half);

            px = Math.max(cx - half + 2, Math.min(cx + half - 2, px));
            pz = Math.max(cy - half + 2, Math.min(cy + half - 2, pz));

            ctx.fill(px - 1, pz - 1, px + 2, pz + 2, dotColor.get());
        }

        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, selfColor.get());
    }
}
