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

public class JetPack extends Module {

    private final IntSetting power = register(new IntSetting("Power", "Thrust power level (1-10)", 5, 1, 10));
    private final BoolSetting needFireworks = register(new BoolSetting("Need Fireworks", "Require fireworks in inventory to fly", false));
    private final BoolSetting noFallDmg = register(new BoolSetting("No Fall Damage", "Reset fall distance while active", true));

    public JetPack() {
        super("JetPack", "Jetpack thrust upward when space pressed while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (!mc.options.jumpKey.isPressed()) return;

        if (needFireworks.isEnabled()) {
            boolean hasFirework = InventoryUtil.findItem(Items.FIREWORK_ROCKET) != -1;
            if (!hasFirework) return;
        }

        double thrust = power.get() * 0.04;
        Vec3d vel = mc.player.getVelocity();
        double maxVY = power.get() * 0.1;
        double newVY = Math.min(vel.y + thrust, maxVY);

        mc.player.setVelocity(vel.x, newVY, vel.z);

        if (noFallDmg.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
