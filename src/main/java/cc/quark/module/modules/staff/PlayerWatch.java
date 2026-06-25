package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerWatch extends Module {
    private final StringSetting targetName = register(new StringSetting("Player", "Player name to watch", ""));
    private final BoolSetting logMovement = register(new BoolSetting("LogMovement", "Log unusual movement", true));
    private final BoolSetting alertOnFly = register(new BoolSetting("AlertOnFly", "Alert when player appears to fly", true));
    private double lastY = 0;
    private int airTicks = 0;
    public PlayerWatch() { super("PlayerWatch", "Monitors a specific player for suspicious behavior", Category.STAFF); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || targetName.getValue().isEmpty()) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!p.getName().getString().equalsIgnoreCase(targetName.getValue())) continue;
            if (alertOnFly.getValue() && !p.isOnGround() && p.getY() > lastY + 0.1) {
                airTicks++;
                if (airTicks > 20) {
                    ChatUtil.warn("[Watch] " + p.getName().getString() + " may be flying!");
                    airTicks = 0;
                }
            } else { airTicks = 0; }
            lastY = p.getY();
        }
    }
}
