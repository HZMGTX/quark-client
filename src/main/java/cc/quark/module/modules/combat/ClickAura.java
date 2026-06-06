package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * ClickAura - attacks the nearest entity only while the left mouse button is
 * held down.  Combines the feel of manual clicking with automated target
 * selection and range checking.
 */
public class ClickAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 4.0, 2.0, 6.0));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only attack other players", false));

    private final BoolSetting requireCooldown = register(new BoolSetting(
            "Require Cooldown", "Wait for full 1.9 attack cooldown", true));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play hand-swing animation", true));

    public ClickAura() {
        super("ClickAura", "Attacks nearby entities only while left-click is held", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only activate while the attack key is physically held
        if (!mc.options.attackKey.isPressed()) return;

        if (requireCooldown.isEnabled() && mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        LivingEntity target = null;
        double best = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist <= range.get() && dist < best) {
                best = dist;
                target = living;
            }
        }

        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);
    }
}
