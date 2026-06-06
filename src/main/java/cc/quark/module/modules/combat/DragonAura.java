package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.Hand;

/**
 * DragonAura — automatically attacks the Ender Dragon and its body parts.
 * Targets body parts directly since the dragon itself is immune to normal damage
 * except at its head.
 */
public class DragonAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 6.0, 1.0, 12.0));
    private final IntSetting attackDelay = register(new IntSetting(
            "Delay", "Milliseconds between attacks", 200, 50, 1000));
    private final BoolSetting headOnly = register(new BoolSetting(
            "Head Only", "Only attack the dragon's head part", false));
    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Swing hand when attacking", true));

    private final TimerUtil timer = new TimerUtil();

    public DragonAura() {
        super("DragonAura", "Automatically attacks the Ender Dragon", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        timer.reset();
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @Override
    public String getSuffix() {
        if (mc.world == null || mc.player == null) return null;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EnderDragonEntity dragon) {
                return String.format("%.0f HP", dragon.getHealth());
            }
        }
        return null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(attackDelay.get())) return;

        double r = range.get();
        Entity bestTarget = null;
        double bestDist = r;

        for (Entity entity : mc.world.getEntities()) {
            // Attack dragon body parts
            if (entity instanceof EnderDragonPart part) {
                if (headOnly.isEnabled() && !part.getName().getString().contains("head")) continue;
                double d = mc.player.distanceTo(part);
                if (d < bestDist) {
                    bestDist = d;
                    bestTarget = part;
                }
            }
            // Also attack the dragon entity directly (head melee range)
            if (!headOnly.isEnabled() && entity instanceof EnderDragonEntity dragon) {
                double d = mc.player.distanceTo(dragon);
                if (d < bestDist) {
                    bestDist = d;
                    bestTarget = dragon;
                }
            }
        }

        if (bestTarget == null) return;

        mc.interactionManager.attackEntity(mc.player, bestTarget);
        if (swing.isEnabled()) mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
