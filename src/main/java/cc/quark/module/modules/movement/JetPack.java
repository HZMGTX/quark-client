package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/**
 * JetPack - holding Jump while airborne applies upward thrust; IntSetting Power
 * (1-10) scales velocity; resets fall distance to prevent fall damage.
 */
public class JetPack extends Module {

    private final IntSetting power = register(new IntSetting(
            "Power", "Thrust power (1-10)", 5, 1, 10));
    private final BoolSetting needFireworks = register(new BoolSetting(
            "Need Fireworks", "Require fireworks in inventory", false));
    private final BoolSetting noFallDmg = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance while thrusting", true));

    public JetPack() {
        super("JetPack", "Upward thrust on Jump key while airborne; Power scales velocity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (!mc.options.jumpKey.isPressed()) return;

        if (needFireworks.isEnabled() && InventoryUtil.findItem(Items.FIREWORK_ROCKET) == -1) return;

        // Thrust: each power unit adds 0.04 per tick; cap proportional to power
        double thrust  = power.get() * 0.04;
        double capVY   = power.get() * 0.08;

        Vec3d vel = mc.player.getVelocity();
        double newVY = Math.min(vel.y + thrust, capVY);

        mc.player.setVelocity(vel.x, newVY, vel.z);

        if (noFallDmg.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
