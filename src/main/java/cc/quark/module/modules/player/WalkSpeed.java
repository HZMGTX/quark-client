package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * WalkSpeed — multiplies the player's horizontal movement speed each tick.
 * Works by directly scaling the velocity vector after vanilla movement is
 * applied, giving a natural-feeling speed boost.
 */
public class WalkSpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Speed multiplier (1.0 = normal)", 1.5, 0.1, 5.0));

    private final BoolSetting onlyGround = register(new BoolSetting(
            "Only On Ground", "Only boost speed while on the ground", false));

    private final BoolSetting onlySprint = register(new BoolSetting(
            "Only While Sprinting", "Only boost speed while sprinting", false));

    private final BoolSetting sneakSlow = register(new BoolSetting(
            "Sneak Slow", "Do not boost speed while sneaking", true));

    public WalkSpeed() {
        super("WalkSpeed", "Adjusts player walk speed", Category.PLAYER);
    }

    @Override
    public String getSuffix() {
        return String.format("%.1fx", speed.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity player = mc.player;

        // Conditions under which we do NOT apply the boost
        if (onlyGround.isEnabled() && !player.isOnGround()) return;
        if (onlySprint.isEnabled() && !player.isSprinting()) return;
        if (sneakSlow.isEnabled() && player.isSneaking()) return;

        // Only boost if the player is actually moving
        double vx = player.getVelocity().x;
        double vz = player.getVelocity().z;
        double horizontalSpeed = Math.sqrt(vx * vx + vz * vz);
        if (horizontalSpeed < 0.001) return;

        double multiplier = speed.get();
        // Clamp the final speed so it can't exceed multiplier * default walk speed (~0.22 BPS)
        double maxSpeed = 0.22 * multiplier;
        double newSpeed = Math.min(horizontalSpeed * multiplier, maxSpeed);
        double scale = newSpeed / horizontalSpeed;

        player.setVelocity(vx * scale, player.getVelocity().y, vz * scale);
    }
}
