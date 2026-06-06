package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class ViolationLog extends Module {
    private final IntSetting flyVL = register(new IntSetting("FlyVL", "Fly violation threshold", 10, 1, 50));
    private final IntSetting speedVL = register(new IntSetting("SpeedVL", "Speed violation threshold", 10, 1, 50));
    private final BoolSetting autoFlag = register(new BoolSetting("AutoFlag", "Auto-flag players over VL threshold", true));
    private final Map<String, Integer> violations = new HashMap<>();

    public ViolationLog() { super("ViolationLog", "Tracks violation levels for players", Category.STAFF); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            String name = p.getName().getString();
            int vl = violations.getOrDefault(name, 0);
            if (!p.isOnGround() && p.getVelocity().y > 0.5) {
                violations.put(name, vl + 1);
                if (autoFlag.getValue() && violations.get(name) >= flyVL.getValue()) {
                    ChatUtil.warn("[VL] " + name + " exceeded fly threshold!");
                    violations.put(name, 0);
                }
            }
        }
    }
}
