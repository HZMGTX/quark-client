package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.Vec3d;

public class BowAimbot extends Module {
    private final DoubleSetting range    = register(new DoubleSetting("Range",       "Max aim range",        48.0, 8.0, 96.0));
    private final BoolSetting   onlyPlyr = register(new BoolSetting  ("Only Players","Only target players",  true));

    public BowAimbot() { super("BowAimbot", "Aims bow at nearest target automatically", Category.COMBAT); }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) return;

        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (var e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity l) || l == mc.player) continue;
            if (onlyPlyr.isEnabled() && !(l instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(l);
            if (d < range.get() && d < best) { best = d; target = l; }
        }
        if (target == null) return;

        double charge = mc.player.getItemUseTime() / 20.0;
        double v = charge * 3.0;
        Vec3d tp = target.getPos().add(0, target.getHeight() / 2.0, 0);
        double dx = tp.x - mc.player.getX();
        double dy = tp.y - mc.player.getEyeY();
        double dz = tp.z - mc.player.getZ();
        double dh = Math.sqrt(dx*dx + dz*dz);
        float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy - dh * 0.1 / Math.max(v, 0.01), dh));
        event.setYaw(yaw);
        event.setPitch(pitch);
    }
}
