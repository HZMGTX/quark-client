package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

public class TargetSelector extends Module {

    private final ModeSetting priority = register(new ModeSetting(
            "Priority", "How to pick between targets", "Distance",
            "Distance", "HP", "LOS", "Name"));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Target search radius in blocks", 6.0, 2.0, 12.0));

    private final BoolSetting requireLOS = register(new BoolSetting(
            "Require LOS", "Skip targets without line of sight", false));

    private LivingEntity currentTarget = null;

    public TargetSelector() {
        super("TargetSelector", "Allows configuring target priority (HP/Distance/LOS/Name)", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        currentTarget = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        List<LivingEntity> candidates = StreamSupport
                .stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e != mc.player)
                .filter(e -> e instanceof PlayerEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> !e.isDead() && e.getHealth() > 0f)
                .filter(e -> mc.player.distanceTo(e) <= range.get())
                .filter(e -> !requireLOS.isEnabled() || mc.player.canSee(e))
                .collect(java.util.stream.Collectors.toList());

        if (candidates.isEmpty()) {
            currentTarget = null;
            return;
        }

        String mode = priority.get();
        switch (mode) {
            case "HP" -> candidates.sort(Comparator.comparingDouble(LivingEntity::getHealth));
            case "LOS" -> candidates.sort(Comparator.comparingDouble(e ->
                    mc.player.canSee(e) ? 0.0 : 1.0));
            case "Name" -> candidates.sort(Comparator.comparing(e ->
                    e instanceof PlayerEntity p ? p.getGameProfile().getName() : ""));
            default -> candidates.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        }

        currentTarget = candidates.get(0);
    }

    public LivingEntity getTarget() {
        return currentTarget;
    }
}
