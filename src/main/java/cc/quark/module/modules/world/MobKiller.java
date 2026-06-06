package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.Comparator;
import java.util.List;

public class MobKiller extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Kill radius for mobs", 5.0, 1.0, 10.0));
    private final IntSetting attackDelay = register(new IntSetting(
            "Attack Delay", "Delay between attacks (ms)", 500, 50, 2000));
    private final BoolSetting passive = register(new BoolSetting(
            "Kill Passive", "Also kill passive mobs (cows, pigs, etc.)", false));
    private final TimerUtil timer = new TimerUtil();

    public MobKiller() {
        super("MobKiller", "Kills mobs in a configurable radius automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(attackDelay.get())) return;

        double r = range.get();
        Box box = mc.player.getBoundingBox().expand(r);

        List<MobEntity> mobs = mc.world.getEntitiesByClass(
                MobEntity.class, box,
                e -> !e.isDead() && mc.player.squaredDistanceTo(e) <= r * r
                        && (passive.isEnabled() || isHostile(e)));

        if (mobs.isEmpty()) return;

        mobs.sort(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)));
        MobEntity target = mobs.get(0);

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }

    private boolean isHostile(MobEntity e) {
        return e instanceof net.minecraft.entity.mob.HostileEntity;
    }
}
