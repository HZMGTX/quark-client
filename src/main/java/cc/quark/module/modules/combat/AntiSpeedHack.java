package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiSpeedHack extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Speed in blocks/tick above which is suspicious", 0.6, 0.2, 2.0));

    private final BoolSetting alert = register(new BoolSetting(
            "Alert", "Show chat alert when speedhacker detected", true));

    private final Map<UUID, Vec3d> lastPositions = new HashMap<>();
    private final Map<UUID, Long> alertCooldowns = new HashMap<>();

    public AntiSpeedHack() {
        super("AntiSpeedHack", "Detects speed-hacking players nearby", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        lastPositions.clear();
        alertCooldowns.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;

            UUID uuid = player.getUuid();
            Vec3d currentPos = player.getPos();
            Vec3d lastPos = lastPositions.get(uuid);

            if (lastPos != null) {
                // Compute horizontal speed in blocks per tick
                double dx = currentPos.x - lastPos.x;
                double dz = currentPos.z - lastPos.z;
                double speed = Math.sqrt(dx * dx + dz * dz);

                if (speed > threshold.get()) {
                    Long lastAlert = alertCooldowns.get(uuid);
                    if (lastAlert == null || now - lastAlert > 3000) {
                        if (alert.isEnabled()) {
                            String name = player.getGameProfile().getName();
                            cc.quark.util.ChatUtil.info("[AntiSpeedHack] " + name +
                                    " may be speed-hacking! Speed: " +
                                    String.format("%.2f", speed * 20) + " BPS");
                        }
                        alertCooldowns.put(uuid, now);
                    }
                }
            }

            lastPositions.put(uuid, currentPos);
        }
    }
}
