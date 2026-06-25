package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class HeadshotAim extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to snap aim", 4.0, 1.0, 8.0));
    private final DoubleSetting snap = register(new DoubleSetting("Snap", "Aim snap strength (0-1)", 0.4, 0.05, 1.0));

    public HeadshotAim() {
        super("HeadshotAim", "Snaps aim to entity head for crits", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);
        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        // Aim at head position
        Vec3d headPos = new Vec3d(target.getX(), target.getY() + target.getHeight(), target.getZ());
        Vec3d eyes = mc.player.getEyePos();

        double dx = headPos.x - eyes.x;
        double dy = headPos.y - eyes.y;
        double dz = headPos.z - eyes.z;

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        float snapFactor = (float) snap.get();
        float newYaw = event.getYaw() + MathHelper.wrapDegrees(targetYaw - event.getYaw()) * snapFactor;
        float newPitch = event.getPitch() + (targetPitch - event.getPitch()) * snapFactor;

        event.setYaw(newYaw);
        event.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }
}
