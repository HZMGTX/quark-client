package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class SpeedCrit extends Module {

    private final BoolSetting onlyOnGround = register(new BoolSetting("OnlyOnGround", "Only perform crit when standing on ground", true));

    public SpeedCrit() {
        super("SpeedCrit", "Performs critical hits by briefly manipulating vertical motion", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;
        if (mc.player.isInLava() || mc.player.isSubmergedInWater() || mc.player.isClimbing()) return;

        Vec3d vel = mc.player.getVelocity();
        // Set a tiny downward velocity to register as a falling crit
        mc.player.setVelocity(vel.x, -0.1, vel.z);
        mc.player.fallDistance = 0.1f;
    }
}
