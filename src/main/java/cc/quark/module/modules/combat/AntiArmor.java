package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class AntiArmor extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to search for low-armor targets", 5.0, 1.0, 10.0));

    private final IntSetting maxArmor = register(new IntSetting(
            "Max Armor", "Maximum armor value to consider target low-armor", 20, 0, 30));

    public AntiArmor() {
        super("AntiArmor", "Targets enemies with low armor", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity bestTarget = null;
        int lowestArmor = maxArmor.get() + 1;
        double r = range.get();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living.isDead()) continue;
            if (mc.player.distanceTo(living) > r) continue;
            if (living instanceof PlayerEntity p && p.getArmor() <= maxArmor.get()) {
                if (p.getArmor() < lowestArmor) {
                    lowestArmor = p.getArmor();
                    bestTarget = living;
                }
            }
        }

        if (bestTarget != null && mc.player.getAttackCooldownProgress(0f) >= 1f) {
            mc.interactionManager.attackEntity(mc.player, bestTarget);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
