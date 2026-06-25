package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class CombatRadar extends Module {

    private final IntSetting radarSize = register(new IntSetting("Radar Size", "Size of the radar in pixels", 80, 40, 200));
    private final IntSetting range = register(new IntSetting("Range", "Detection range in blocks", 64, 16, 256));

    public CombatRadar() {
        super("CombatRadar", "Shows radar of nearby players on HUD", Category.COMBAT);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int size = radarSize.get();
        int x = screenW - size - 4;
        int y = 4;
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int r = range.get();

        // Background
        ctx.fill(x, y, x + size, y + size, 0xAA000000);
        // Border
        ctx.drawBorder(x, y, size, size, 0xFF555555);

        // Center dot (self)
        ctx.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFF00FF00);

        Vec3d playerPos = mc.player.getPos();
        float playerYaw = mc.player.getYaw();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity other)) continue;
            if (other == mc.player) continue;
            if (EntityUtil.isFriend(other)) continue;

            double dx = other.getX() - playerPos.x;
            double dz = other.getZ() - playerPos.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > r) continue;

            // Rotate relative to player yaw
            double angle = Math.toRadians(playerYaw);
            double rotX = dx * Math.cos(angle) - dz * Math.sin(angle);
            double rotZ = dx * Math.sin(angle) + dz * Math.cos(angle);

            int dotX = centerX + (int) (rotX / r * (size / 2));
            int dotY = centerY + (int) (rotZ / r * (size / 2));

            // Clamp within radar
            dotX = Math.max(x + 2, Math.min(x + size - 2, dotX));
            dotY = Math.max(y + 2, Math.min(y + size - 2, dotY));

            ctx.fill(dotX - 1, dotY - 1, dotX + 1, dotY + 1, 0xFFFF4444);
        }
    }
}
