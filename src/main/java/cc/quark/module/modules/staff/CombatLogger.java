package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.DoubleSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLogger extends Module {

    private final DoubleSetting range   = new DoubleSetting("Range", 32, 8, 128);
    private final BoolSetting logDeaths = new BoolSetting("LogDeaths", true);
    private final BoolSetting logLowHP  = new BoolSetting("LogLowHP", true);

    private final Map<UUID, Float> prevHealth = new HashMap<>();

    public CombatLogger() {
        super("CombatLogger", "Logs combat events: deaths, low HP, damage spikes for nearby players", Category.STAFF);
        addSettings(range, logDeaths, logLowHP);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); prevHealth.clear(); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null || mc.world == null) return;

        mc.world.getPlayers().forEach(p -> {
            if (p == mc.player || p.getPos().distanceTo(mc.player.getPos()) > range.get()) return;
            UUID id = p.getUuid();
            float hp = p.getHealth();
            float prev = prevHealth.getOrDefault(id, hp);

            if (logDeaths.isEnabled() && !p.isAlive() && prev > 0) {
                log("§c[DEATH] §f" + p.getGameProfile().getName() + " died");
            }
            if (logLowHP.isEnabled() && hp <= 4 && prev > 4) {
                log("§e[LOWhp] §f" + p.getGameProfile().getName() + " §cat " + String.format("%.1f", hp) + "hp");
            }
            prevHealth.put(id, hp);
        });
    }

    private void log(String msg) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        mc.player.sendMessage(Text.literal("§6[CombatLog] " + msg), false);
    }
}
