package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class AntiAFK2 extends Module {

    private final ModeSetting mode     = register(new ModeSetting("Mode",     "Anti-AFK movement type",      "Turn", "Turn", "Walk", "Jump", "Spin"));
    private final IntSetting  interval = register(new IntSetting ("Interval", "Seconds between actions",     30, 5, 120));

    private final TimerUtil timer = new TimerUtil();
    private int spinTick = 0;

    public AntiAFK2() {
        super("AntiAFK2", "Prevents AFK kick with periodic movement actions", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(interval.get() * 1000L)) return;

        switch (mode.get()) {
            case "Turn" -> {
                spinTick++;
                mc.player.setYaw(mc.player.getYaw() + 90f);
            }
            case "Walk" -> {
                Vec3d look = mc.player.getRotationVec(1.0f);
                mc.player.addVelocity(look.x * 0.2, 0, look.z * 0.2);
            }
            case "Jump" -> {
                if (mc.player.isOnGround()) mc.player.jump();
            }
            case "Spin" -> {
                mc.player.setYaw(mc.player.getYaw() + 45f);
                return; // spin is continuous, don't reset timer
            }
        }
        timer.reset();
    }
}
