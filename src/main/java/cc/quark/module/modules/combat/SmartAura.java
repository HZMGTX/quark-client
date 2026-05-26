package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * SmartAura - attacks the lowest-health nearby target within range.
 */
public class SmartAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between attacks", 8, 0, 20));
    private final BoolSetting onlyPlayers = register(new BoolSetting("Only Players", "Target only players", false));

    private int ticks;

    public SmartAura() {
        super("SmartAura", "Attacks the lowest-health nearby target", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ticks++;
        if (ticks < delay.get()) return;

        LivingEntity best = null;
        float bestHealth = Float.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (entity instanceof PlayerEntity player
                    && Quark.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            if (living.getHealth() < bestHealth) {
                bestHealth = living.getHealth();
                best = living;
            }
        }

        if (best != null) {
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
            ticks = 0;
        }
    }
}
