package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AntiTotem extends Module {

    private final IntSetting predictMs = register(new IntSetting(
            "Predict Ms", "Pre-attack timing before predicted totem pop in milliseconds", 100, 0, 500));

    private final IntSetting range = register(new IntSetting(
            "Range", "Attack range in blocks", 4, 2, 6));

    private final TimerUtil timer = new TimerUtil();

    public AntiTotem() {
        super("AntiTotem", "Predicts when enemy is about to pop and pre-positions attack", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(predictMs.get())) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1f) return;

        PlayerEntity target = findCriticalTarget();
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }

    private PlayerEntity findCriticalTarget() {
        PlayerEntity best = null;
        double bestHealth = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(p) > range.get()) continue;

            boolean hasTotem = holdsTotem(p);
            if (!hasTotem) continue;

            if (p.getHealth() < bestHealth) {
                bestHealth = p.getHealth();
                best = p;
            }
        }
        return best;
    }

    private boolean holdsTotem(PlayerEntity player) {
        return player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
                || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
    }
}
