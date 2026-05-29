package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MobAura — attacks nearby hostile mobs.
 * Modes: Closest (nearest mob first) | Weakest (lowest HP first).
 */
public class MobAura extends Module {

    private final DoubleSetting range  = register(new DoubleSetting("Range",  "Attack range",                  3.5, 1.0, 6.0));
    private final IntSetting    speed  = register(new IntSetting   ("Speed",  "Ms between attacks",            200, 50, 1000));
    private final ModeSetting   mode   = register(new ModeSetting  ("Mode",   "Target selection mode",         "Closest", "Closest", "Weakest"));

    private final TimerUtil timer = new TimerUtil();

    public MobAura() {
        super("MobAura", "Automatically attacks nearby hostile mobs", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return mode.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(speed.get())) return;

        List<MobEntity> mobs = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof MobEntity mob)) continue;
            if (mob.isDead() || mob.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(mob) > range.get()) continue;
            mobs.add(mob);
        }
        if (mobs.isEmpty()) return;

        if (mode.is("Weakest")) {
            mobs.sort(Comparator.comparingDouble(LivingEntity::getHealth));
        } else {
            mobs.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        }

        MobEntity target = mobs.get(0);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
