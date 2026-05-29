package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

/**
 * KnifeSpeed - sprint burst: when attacking an entity, briefly boost horizontal
 * speed forward for N ticks before returning to normal.
 */
public class KnifeSpeed extends Module {

    private final DoubleSetting boostAmount = register(new DoubleSetting(
            "Boost Amount", "Forward velocity applied on attack", 0.8, 0.1, 3.0));
    private final IntSetting duration = register(new IntSetting(
            "Duration", "Number of ticks to maintain the burst", 5, 1, 20));

    private int boostTicksRemaining = 0;

    public KnifeSpeed() {
        super("KnifeSpeed", "Sprint burst on attack — brief forward speed boost", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        boostTicksRemaining = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        boostTicksRemaining = duration.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (boostTicksRemaining <= 0) return;

        boostTicksRemaining--;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double bx = -Math.sin(yaw) * boostAmount.get();
        double bz =  Math.cos(yaw) * boostAmount.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(bx, vel.y, bz);
        mc.player.setSprinting(true);
    }
}
