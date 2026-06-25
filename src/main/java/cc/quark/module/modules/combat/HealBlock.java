package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class HealBlock extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect regenerating enemies", 5.0, 1.0, 10.0));

    public HealBlock() {
        super("HealBlock", "Prevents enemies from healing (damages during regen)", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < 1f) return;

        double r = range.get();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living.isRemoved()) continue;
            if (mc.player.distanceTo(living) > r) continue;
            // Attack enemies that have regeneration or saturation healing
            if (living.hasStatusEffect(StatusEffects.REGENERATION)
                    || living.hasStatusEffect(StatusEffects.SATURATION)) {
                mc.interactionManager.attackEntity(mc.player, living);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }
}
