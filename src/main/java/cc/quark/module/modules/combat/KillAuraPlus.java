package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class KillAuraPlus extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 3.5, 2.0, 6.0));
    private final IntSetting speed = register(new IntSetting("Speed", "Milliseconds between attacks", 200, 50, 1000));
    private final BoolSetting rotate = register(new BoolSetting("Rotate", "Silently rotate to target before attacking", true));
    private final BoolSetting multiTarget = register(new BoolSetting("Multi Target", "Attack multiple targets per tick", false));

    private final TimerUtil timer = new TimerUtil();
    private LivingEntity target = null;

    private float serverYaw = 0f;
    private float serverPitch = 0f;

    public KillAuraPlus() {
        super("KillAuraPlus", "Enhanced kill aura with rotation bypass", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        target = null;
        timer.reset();
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (!rotate.isEnabled() || target == null) return;
        if (mc.player == null) return;

        Vec3d eyePos = target.getEyePos();
        serverYaw = RotationUtil.getYaw(eyePos);
        serverPitch = RotationUtil.getPitch(eyePos);

        event.setYaw(serverYaw);
        event.setPitch(MathHelper.clamp(serverPitch, -90f, 90f));
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(speed.get())) return;

        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < 0.9f) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);
        if (targets.isEmpty()) {
            target = null;
            return;
        }

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        target = targets.get(0);

        int count = multiTarget.isEnabled() ? targets.size() : 1;
        for (int i = 0; i < count; i++) {
            LivingEntity t = targets.get(i);
            mc.interactionManager.attackEntity(mc.player, t);
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        timer.reset();
    }
}
