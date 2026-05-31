package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * SilentBow - silently aims the bow at the nearest player without visibly moving the
 * camera, then auto-releases when the charge is sufficient.
 *
 * <p>"Silent" means rotation packets are sent to the server but the client camera
 * stays in its original position.
 */
public class SilentBow extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum target distance in blocks", 40.0, 5.0, 80.0));

    private final IntSetting minCharge = register(new IntSetting(
            "Min Charge", "Ticks of charge before auto-release (20 = full power)", 20, 5, 20));

    private final BoolSetting predictMovement = register(new BoolSetting(
            "Predict", "Account for target movement when aiming", true));

    private final DoubleSetting aimSpeed = register(new DoubleSetting(
            "Aim Speed", "How quickly to rotate toward the target (0 = instant)", 0.5, 0.0, 1.0));

    private int chargeTicks = 0;
    private float serverYaw = 0f;
    private float serverPitch = 0f;

    public SilentBow() {
        super("SilentBow", "Silently aims bow and auto-releases at nearest player", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        chargeTicks = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (mc.player.isUsingItem()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        chargeTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Must be holding a bow
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) {
            chargeTicks = 0;
            return;
        }

        // Must have arrows
        boolean hasArrow = false;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ARROW
                    || mc.player.getInventory().getStack(i).getItem() == Items.TIPPED_ARROW
                    || mc.player.getInventory().getStack(i).getItem() == Items.SPECTRAL_ARROW) {
                hasArrow = true;
                break;
            }
        }
        if (!hasArrow) return;

        LivingEntity target = findNearestTarget();
        if (target == null) {
            if (mc.player.isUsingItem()) {
                mc.interactionManager.stopUsingItem(mc.player);
            }
            chargeTicks = 0;
            return;
        }

        // Compute aim angles toward target (optionally predicting movement)
        Vec3d aimPos = getAimPosition(target);
        Vec3d eyePos = mc.player.getEyePos();
        double dx = aimPos.x - eyePos.x;
        double dy = aimPos.y - eyePos.y;
        double dz = aimPos.z - eyePos.z;

        float targetYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        targetPitch = MathHelper.clamp(targetPitch, -90f, 90f);

        // Smooth rotation toward target
        double t = 1.0 - aimSpeed.get();
        serverYaw   = lerpAngle(serverYaw,   targetYaw,   (float) t);
        serverPitch = lerp(serverPitch, targetPitch, (float) t);

        // Send silent rotation packet to server without moving client camera
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                serverYaw, serverPitch, mc.player.isOnGround()));

        // Start charging if not already
        if (!mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            chargeTicks = 0;
        } else {
            chargeTicks++;
            if (chargeTicks >= minCharge.get()) {
                mc.interactionManager.stopUsingItem(mc.player);
                chargeTicks = 0;
            }
        }
    }

    private Vec3d getAimPosition(LivingEntity target) {
        Vec3d pos = target.getEyePos();
        if (predictMovement.isEnabled()) {
            // Simple linear prediction: offset by velocity scaled by travel time estimate
            double dist = mc.player.getEyePos().distanceTo(pos);
            double travelTicks = dist / 3.0; // rough arrow speed approximation
            Vec3d vel = target.getVelocity();
            pos = pos.add(vel.x * travelTicks, vel.y * travelTicks * 0.5, vel.z * travelTicks);
        }
        return pos;
    }

    private LivingEntity findNearestTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private float lerpAngle(float from, float to, float t) {
        float diff = MathHelper.wrapDegrees(to - from);
        return from + diff * t;
    }

    private float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }
}
