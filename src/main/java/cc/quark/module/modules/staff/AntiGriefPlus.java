package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiGriefPlus extends Module {

    private final BoolSetting alertOnFlying   = register(new BoolSetting("AlertFlying", "AlertFlying", true));
    private final BoolSetting alertOnSpeed    = register(new BoolSetting("AlertSpeed", "AlertSpeed", true));
    private final BoolSetting alertOnExplode  = register(new BoolSetting("AlertExplosion", "AlertExplosion", true));
    private final IntSetting alertCooldown    = register(new IntSetting("Cooldown", "Cooldown", 100, 20, 400));

    private final Map<UUID, Long> lastAlert = new HashMap<>();
    private final Map<UUID, Float> lastY    = new HashMap<>();

    public AntiGriefPlus() {
        super("AntiGriefPlus", "Enhanced anti-grief: flags flying, speed, and explosion abuse", Category.STAFF);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
        if (mc == null || mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            UUID id = p.getUuid();

            boolean canAlert = !lastAlert.containsKey(id)
                || now - lastAlert.get(id) > alertCooldown.get() * 50L;
            if (!canAlert) continue;

            float prevY = lastY.getOrDefault(id, (float) p.getY());
            float dy = (float)(p.getY() - prevY);
            lastY.put(id, (float) p.getY());

            if (alertOnFlying.isEnabled() && dy > 0.3f && !p.hasVehicle() && !p.isOnGround()) {
                flag(p, "flying (dY=" + String.format("%.2f", dy) + ")");
                lastAlert.put(id, now);
            }
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming() || !alertOnExplode.isEnabled()) return;
        String msg = event.getMessage();
        if (msg != null && (msg.toLowerCase().contains("explod") || msg.toLowerCase().contains("tnt"))) {
            alert("Possible explosion event detected in chat");
        }
    }

    private void flag(PlayerEntity p, String reason) {
        alert("§c" + p.getGameProfile().getName() + " §fflagged: " + reason);
    }

    private void alert(String msg) {
        
        if (mc == null || mc.player == null) return;
        mc.player.sendMessage(Text.literal("§6[AntiGrief+] §f" + msg), false);
    }
}
