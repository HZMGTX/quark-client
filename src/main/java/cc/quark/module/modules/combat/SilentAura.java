package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * SilentAura — attacks nearby entities without rotating the visible player model.
 * Server-side yaw/pitch are spoofed via EventPreMotion; the camera stays put.
 * Smooth setting controls how fast the server-side rotation transitions.
 */
public class SilentAura extends Module {

    private final DoubleSetting range       = register(new DoubleSetting("Range",       "Attack range",                     3.5, 1.0, 6.0));
    private final IntSetting    speed       = register(new IntSetting   ("Speed",       "Ms between attacks",               200, 50, 1000));
    private final BoolSetting   onlyPlayers = register(new BoolSetting  ("Only Players","Only target players",               false));

    private float serverYaw   = 0f;
    private float serverPitch = 0f;
    private boolean hasTarget  = false;
    private long lastAttack   = 0L;

    public SilentAura() {
        super("SilentAura", "Silent-aim attacks: server sees yaw toward target, client head stays still", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        hasTarget = false;
        if (mc.player != null) {
            serverYaw   = mc.player.getYaw();
            serverPitch = mc.player.getPitch();
        }
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f", range.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity target = findTarget();
        if (target == null) { hasTarget = false; return; }
        hasTarget = true;

        // Compute angles toward target
        Vec3d eye = target.getEyePos();
        serverYaw   = RotationUtil.getYaw(eye);
        serverPitch = MathHelper.clamp(RotationUtil.getPitch(eye), -90f, 90f);

        // Attack if cooldown allows
        long now = System.currentTimeMillis();
        if (now - lastAttack >= speed.get()) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = now;
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (!hasTarget) return;
        // Override the packet yaw/pitch without touching mc.player yaw/pitch
        event.setYaw(serverYaw);
        event.setPitch(serverPitch);
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (Quark.getInstance() != null && entity instanceof PlayerEntity p
                    && Quark.getInstance().getFriendManager().isFriend(p.getGameProfile().getName())) continue;
            double d = mc.player.distanceTo(entity);
            if (d < bestDist) { bestDist = d; best = living; }
        }
        return best;
    }
}
