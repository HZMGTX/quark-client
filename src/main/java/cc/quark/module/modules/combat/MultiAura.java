package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.RotationManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MultiAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 2.0, 6.0));

    private final IntSetting maxTargets = register(new IntSetting(
            "Max Targets", "Maximum targets to hit per cycle", 3, 1, 8));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between target switches within a cycle", 0, 0, 500));

    private final BoolSetting rotate = register(new BoolSetting(
            "Rotate", "Silently rotate toward the primary (closest) target", true));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", false));

    private long lastCycleMs = 0;
    private int targetIndex = 0;

    public MultiAura() {
        super("MultiAura", "Attacks multiple targets per tick, sorted by distance", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastCycleMs = 0;
        targetIndex = 0;
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f | %d", range.get(), maxTargets.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        List<LivingEntity> targets = collectTargets();
        if (targets.isEmpty()) return;

        if (rotate.isEnabled() && !targets.isEmpty()) {
            Vec3d primaryEye = targets.get(0).getEyePos();
            float yaw = RotationUtil.getYaw(primaryEye);
            float pitch = MathHelper.clamp(RotationUtil.getPitch(primaryEye), -90f, 90f);
            RotationManager.INSTANCE.requestRotation(yaw, pitch, 8, true);
        }

        long now = System.currentTimeMillis();
        int delayMs = delay.get();

        if (delayMs == 0) {
            int count = Math.min(targets.size(), maxTargets.get());
            for (int i = 0; i < count; i++) {
                attack(targets.get(i));
            }
        } else {
            if (now - lastCycleMs >= delayMs) {
                if (targetIndex >= targets.size() || targetIndex >= maxTargets.get()) {
                    targetIndex = 0;
                }
                if (targetIndex < targets.size()) {
                    attack(targets.get(targetIndex));
                    targetIndex++;
                    if (targetIndex >= Math.min(targets.size(), maxTargets.get())) {
                        targetIndex = 0;
                    }
                }
                lastCycleMs = now;
            }
        }
    }

    private void attack(LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        RotationManager.INSTANCE.notifyAttacked();
    }

    private List<LivingEntity> collectTargets() {
        List<LivingEntity> list = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (entity instanceof PlayerEntity player
                    && Quark.getInstance() != null
                    && Quark.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
            if (EntityUtil.distanceTo(entity) > range.get()) continue;
            list.add(living);
        }
        list.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        return list;
    }
}
