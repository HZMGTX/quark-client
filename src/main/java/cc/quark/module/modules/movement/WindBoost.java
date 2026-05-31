package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class WindBoost extends Module {

    private final BoolSetting autoUse = register(new BoolSetting(
            "AutoUse", "Automatically use wind charge when jump key is pressed", true));

    private final DoubleSetting burstStrength = register(new DoubleSetting(
            "Burst Strength", "Horizontal burst velocity added on trigger", 0.8, 0.1, 3.0));

    public WindBoost() {
        super("WindBoost", "Uses mace wind charges for burst movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (!autoUse.isEnabled()) return;
        if (!mc.options.jumpKey.isPressed()) return;
        if (!hasWindCharge()) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double len = (fwd == 0 && side == 0) ? 1.0 : Math.sqrt(fwd * fwd + side * side);
        double nFwd = (fwd == 0 && side == 0) ? 1.0 : fwd / len;
        double nSide = (fwd == 0 && side == 0) ? 0.0 : side / len;

        double burst = burstStrength.get();
        double dx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * burst;
        double dz = (Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * burst;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + dx, vel.y + 0.5, vel.z + dz);
        mc.player.fallDistance = 0;

        // Consume the wind charge by using it
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private boolean hasWindCharge() {
        var main = mc.player.getMainHandStack();
        if (main.getItem() == Items.WIND_CHARGE) return true;
        var off = mc.player.getOffHandStack();
        return off.getItem() == Items.WIND_CHARGE;
    }
}
