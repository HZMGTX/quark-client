package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class SpeedCheck extends Module {

    private final DoubleSetting maxSpeedBps = register(new DoubleSetting(
            "Max Speed (BPS)", "Max allowed blocks-per-second before flagging", 8.0, 1.0, 40.0));
    private final IntSetting flagsToAction = register(new IntSetting(
            "Flags To Action", "Consecutive over-speed ticks before acting", 5, 1, 20));
    private final BoolSetting alertOnly = register(new BoolSetting(
            "Alert Only", "Only alert instead of kicking flagged players", true));
    private final BoolSetting ignoreFlight = register(new BoolSetting(
            "Ignore Flight", "Skip players that appear to be legitimately flying", false));

    private final Map<String, Vec3d> lastPos = new HashMap<>();
    private final Map<String, Integer> flagCount = new HashMap<>();

    public SpeedCheck() {
        super("SpeedCheck", "Detects and flags players moving at impossible speeds", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        lastPos.clear();
        flagCount.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[SpeedCheck] §fMonitoring player movement (max §e" + maxSpeedBps.get() + " BPS§f).");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        lastPos.clear();
        flagCount.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            String name = player.getName().getString();
            Vec3d pos = player.getPos();

            if (ignoreFlight.isEnabled() && player.getAbilities().flying) {
                lastPos.put(name, pos);
                continue;
            }

            if (lastPos.containsKey(name)) {
                Vec3d prev = lastPos.get(name);
                double dx = pos.x - prev.x;
                double dz = pos.z - prev.z;
                // Horizontal distance per tick, convert to BPS (20 ticks/s)
                double dist = Math.sqrt(dx * dx + dz * dz) * 20.0;

                if (dist > maxSpeedBps.get()) {
                    int flags = flagCount.getOrDefault(name, 0) + 1;
                    flagCount.put(name, flags);

                    if (flags >= flagsToAction.get()) {
                        flagCount.put(name, 0);
                        String msg = String.format("%.1f BPS", dist);
                        if (alertOnly.isEnabled()) {
                            ChatUtil.info("§6[SpeedCheck] §cFlagged §e" + name + " §7— §c" + msg);
                        } else {
                            ChatUtil.info("§6[SpeedCheck] §cKicking §e" + name + " §7— " + msg);
                            mc.player.networkHandler.sendChatCommand("kick " + name + " Speed hacking (" + msg + ")");
                        }
                    }
                } else {
                    flagCount.put(name, 0);
                }
            }

            lastPos.put(name, pos);
        }
    }
}
