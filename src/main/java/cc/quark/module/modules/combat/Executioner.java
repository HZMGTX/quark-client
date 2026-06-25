package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class Executioner extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 4.0, 1.0, 6.0));
    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "Target HP below which to execute", 6.0, 1.0, 10.0));
    private final BoolSetting wTap = register(new BoolSetting("W-Tap", "Sprint-release for extra knockback on execution", true));

    private int wTapTicks = 0;
    private boolean strengthActive = false;

    public Executioner() {
        super("Executioner", "Extra damage boost against low-health targets", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        wTapTicks = 0;
        strengthActive = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && strengthActive) {
            mc.player.removeStatusEffect(StatusEffects.STRENGTH);
            strengthActive = false;
        }
        if (mc.player != null && wTapTicks > 0) {
            mc.options.forwardKey.setPressed(false);
            wTapTicks = 0;
        }
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (wTap.isEnabled() && mc.player.isSprinting()) {
            mc.options.forwardKey.setPressed(false);
            mc.player.setSprinting(false);
            wTapTicks = 2;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (wTapTicks > 0 && --wTapTicks == 0) {
            mc.options.forwardKey.setPressed(mc.player.input.movementForward > 0);
            mc.player.setSprinting(true);
        }

        if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        LivingEntity best = null;
        float bestHealth = Float.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            if (living.getHealth() >= (float) threshold.get()) continue;
            if (living.getHealth() < bestHealth) {
                bestHealth = living.getHealth();
                best = living;
            }
        }

        if (best != null) {
            if (!mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false));
                strengthActive = true;
            }
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            if (strengthActive && mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                mc.player.removeStatusEffect(StatusEffects.STRENGTH);
                strengthActive = false;
            }
        }
    }
}
