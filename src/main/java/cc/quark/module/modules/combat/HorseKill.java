package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class HorseKill extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range for mounted animals", 5.0, 1.0, 8.0));

    public HorseKill() {
        super("HorseKill", "Dismounts enemy horses by attacking the horse", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < 1.0f) return;

        for (Entity entity : mc.world.getEntities()) {
            // We want to find horses/animals being ridden by a player
            if (!(entity instanceof AbstractHorseEntity horse)) continue;

            // Check if any passenger is a player
            boolean riddenByPlayer = false;
            for (Entity passenger : horse.getPassengerList()) {
                if (passenger instanceof PlayerEntity rider && rider != mc.player) {
                    riddenByPlayer = true;
                    break;
                }
            }

            if (!riddenByPlayer) continue;

            double dist = mc.player.distanceTo(horse);
            if (dist > range.get()) continue;

            if (!mc.player.canSee(horse)) continue;

            // Attack the horse to dismount the rider
            mc.interactionManager.attackEntity(mc.player, horse);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }
    }
}
