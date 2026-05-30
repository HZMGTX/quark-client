package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.Vec3d;

/**
 * FlyBoost - Provides a burst of speed while flying (creative or elytra)
 * when the sprint key is pressed.
 */
public class FlyBoost extends Module {

    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Velocity multiplier during boost burst", 2.0, 1.1, 5.0));
    private final IntSetting boostTicks = register(new IntSetting("BoostTicks", "Duration of the boost in ticks", 10, 1, 40));

    private final TimerUtil cooldown = new TimerUtil();
    private int boostTicksRemaining = 0;

    public FlyBoost() {
        super("FlyBoost", "Burst speed boost while flying when sprint key is pressed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        boostTicksRemaining = 0;
        cooldown.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        PlayerAbilities abilities = mc.player.getAbilities();
        boolean isFlying = abilities.flying || mc.player.isFallFlying();
        if (!isFlying) return;

        boolean sprintPressed = mc.options.sprintKey.isPressed();

        // Trigger a new boost burst
        if (sprintPressed && boostTicksRemaining <= 0 && cooldown.hasReached(1500)) {
            boostTicksRemaining = boostTicks.get();
            cooldown.reset();
        }

        // Apply boost
        if (boostTicksRemaining > 0) {
            Vec3d vel = mc.player.getVelocity();
            double mult = boost.get();

            // Boost in the look direction while flying
            Vec3d look = mc.player.getRotationVec(1.0f);
            double spd = look.length() > 0 ? mult * 0.3 : 0;

            mc.player.setVelocity(
                    look.x * spd + vel.x * 0.8,
                    look.y * spd + vel.y * 0.8,
                    look.z * spd + vel.z * 0.8
            );

            boostTicksRemaining--;
        }
    }
}
