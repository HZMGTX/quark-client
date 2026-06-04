package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Random;

public class DragClick extends Module {

    private final IntSetting minCps = register(new IntSetting("Min CPS", "Minimum clicks per second", 16, 1, 30));
    private final IntSetting maxCps = register(new IntSetting("Max CPS", "Maximum clicks per second", 20, 1, 40));
    private final BoolSetting onlyOnTarget = register(new BoolSetting("Only On Target", "Only click when a target is in range", true));

    private long lastClickTime = 0L;
    private final Random random = new Random();

    public DragClick() {
        super("DragClick", "Simulates drag clicking CPS", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastClickTime = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (onlyOnTarget.isEnabled()) {
            List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, 4.0);
            targets.removeIf(e -> e == mc.player || !(e instanceof PlayerEntity));
            if (targets.isEmpty()) return;
        }

        int cps = minCps.get() + random.nextInt(Math.max(1, maxCps.get() - minCps.get() + 1));
        long delay = 1000L / cps;

        if (System.currentTimeMillis() - lastClickTime >= delay) {
            // Simulate attack on nearest target
            List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, 4.0);
            targets.removeIf(e -> e == mc.player || e.isDead());
            targets.removeIf(e -> !(e instanceof PlayerEntity));

            if (!targets.isEmpty()) {
                LivingEntity target = targets.stream()
                        .min((a, b) -> Double.compare(EntityUtil.distanceTo(a), EntityUtil.distanceTo(b)))
                        .orElse(null);
                if (target != null) {
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
            lastClickTime = System.currentTimeMillis();
        }
    }
}
