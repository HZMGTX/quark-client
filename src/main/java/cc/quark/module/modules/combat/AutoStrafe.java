package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class AutoStrafe extends Module {

    private final IntSetting switchTicks = register(new IntSetting(
            "Switch Ticks", "Ticks before switching strafe direction", 10, 5, 20));
    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Strafe movement speed", 0.3, 0.1, 1.0));
    private final BoolSetting requireKa = register(new BoolSetting(
            "Require KA Target", "Only strafe when KillAura has a target", true));

    private int tickCount = 0;
    private boolean strafeRight = true;

    public AutoStrafe() {
        super("AutoStrafe", "Auto-strafes around enemies in combat", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tickCount = 0;
        strafeRight = true;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Entity target = null;
        if (requireKa.isEnabled()) {
            KillAura ka = Quark.getInstance().getModuleManager().getModule(KillAura.class);
            if (ka == null || !ka.isEnabled()) return;
            target = ka.getTarget();
            if (target == null) return;
        } else {
            double best = 8.0;
            for (Entity e : mc.world.getEntities()) {
                if (e == mc.player) continue;
                double d = mc.player.distanceTo(e);
                if (d < best) {
                    best = d;
                    target = e;
                }
            }
        }

        if (target == null) return;

        tickCount++;
        if (tickCount >= switchTicks.get()) {
            strafeRight = !strafeRight;
            tickCount = 0;
        }

        double tx = target.getX();
        double tz = target.getZ();
        double dx = mc.player.getX() - tx;
        double dz = mc.player.getZ() - tz;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return;

        double normX = dx / len;
        double normZ = dz / len;

        double perpX = strafeRight ? normZ : -normZ;
        double perpZ = strafeRight ? -normX : normX;

        double s = speed.get();
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + perpX * s, vel.y, vel.z + perpZ * s);
    }

    @Override
    public String getSuffix() {
        return strafeRight ? "Right" : "Left";
    }
}
