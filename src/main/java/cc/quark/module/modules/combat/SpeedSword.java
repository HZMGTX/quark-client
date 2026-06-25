package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * SpeedSword - bypasses the 1.9 attack cooldown by spoofing the internal
 * attack cooldown progress.  Sets the player's attack cooldown counter to
 * zero every tick so that {@code getAttackCooldownProgress} always returns 1.0,
 * allowing immediate back-to-back attacks.
 *
 * Note: purely client-side; server-side NCP will still rate-limit damage.
 * Pair with a reach module for best results on 1.8-compat servers.
 */
public class SpeedSword extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 2.0, 6.0));

    private final DoubleSetting attacksPerSecond = register(new DoubleSetting(
            "Attacks/s", "Target attacks per second (1-20)", 8.0, 1.0, 20.0));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Only target other players", false));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play hand-swing animation", true));

    private int tickAccum = 0;

    public SpeedSword() {
        super("SpeedSword", "Attacks rapidly by resetting the attack cooldown each tick", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tickAccum = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Throttle to configured attacks per second (20 ticks = 1 second)
        tickAccum++;
        int ticksNeeded = Math.max(1, (int) Math.round(20.0 / attacksPerSecond.get()));
        if (tickAccum < ticksNeeded) return;
        tickAccum = 0;

        // Reset attack cooldown so getAttackCooldownProgress returns 1.0
        mc.player.resetLastAttackedTicks();

        // Find nearest target
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d <= range.get() && d < best) { best = d; target = living; }
        }
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);
    }
}
