package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ClutchSilentAim extends Module {

    private final DoubleSetting range       = register(new DoubleSetting("Range",       "Attack range",                3.5, 1.0, 6.0));
    private final IntSetting    speed       = register(new IntSetting   ("Speed",       "Ms between attacks",          250, 50, 1500));
    private final DoubleSetting healthThr   = register(new DoubleSetting("Health Thr",  "Activate below this HP",      8.0, 1.0, 20.0));
    private final BoolSetting   alwaysOn    = register(new BoolSetting  ("Always On",   "Ignore health threshold",     false));
    private final BoolSetting   onlyPlayers = register(new BoolSetting  ("Only Players","Only target players",         true));
    private final DoubleSetting smooth      = register(new DoubleSetting("Smooth",      "Rotation interpolation speed",0.8, 0.1, 1.0));

    private float serverYaw, serverPitch;
    private long lastAttack = 0;

    public ClutchSilentAim() {
        super("ClutchSilentAim", "Activates silent aim when your HP is critically low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) { serverYaw = mc.player.getYaw(); serverPitch = mc.player.getPitch(); }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        event.setYaw(serverYaw);
        event.setPitch(serverPitch);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        float hp = mc.player.getHealth();
        boolean clutching = alwaysOn.isEnabled() || hp <= (float) healthThr.get();
        if (!clutching) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        Vec3d delta = target.getPos().add(0, target.getHeight() / 2.0, 0).subtract(mc.player.getEyePos());
        float yaw   = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));

        float s = (float) smooth.get();
        serverYaw   = lerpAngle(serverYaw, yaw, s);
        serverPitch = MathHelper.lerp(s, serverPitch, MathHelper.clamp(pitch, -90, 90));

        long now = System.currentTimeMillis();
        if (now - lastAttack >= speed.get()) {
            if (mc.player.distanceTo(target) <= range.get()) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                lastAttack = now;
            }
        }
    }

    private LivingEntity findTarget() {
        LivingEntity nearest = null;
        double best = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le == mc.player) continue;
            if (le.isDead() || le.getHealth() <= 0) continue;
            if (onlyPlayers.isEnabled() && !(le instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(le);
            if (d < best) { best = d; nearest = le; }
        }
        return nearest;
    }

    private float lerpAngle(float from, float to, float t) {
        float diff = MathHelper.wrapDegrees(to - from);
        return from + diff * t;
    }
}
