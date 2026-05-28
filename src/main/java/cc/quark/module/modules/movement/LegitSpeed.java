package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class LegitSpeed extends Module {

    private final BoolSetting checkGround = register(new BoolSetting("Check Ground", "Only apply boost when on ground", true));

    private boolean applyThisTick = false;

    public LegitSpeed() {
        super("LegitSpeed", "NCP-safe subtle speed boost applied every other tick", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        applyThisTick = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (checkGround.isEnabled() && !mc.player.isOnGround()) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        applyThisTick = !applyThisTick;
        if (!applyThisTick) return;

        Vec3d vel = mc.player.getVelocity();
        double horizLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (horizLen <= 0) return;

        double boost = 0.4;
        double scale = (horizLen + boost) / horizLen;
        mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
    }
}
