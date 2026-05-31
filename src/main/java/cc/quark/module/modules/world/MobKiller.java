package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

public class MobKiller extends Module {

    private final ModeSetting type = register(new ModeSetting(
            "Type", "Which mob type to kill",
            "All", "All", "Cow", "Chicken", "Pig"));

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to search for mobs", 5, 1, 12));

    private final TimerUtil timer = new TimerUtil();

    public MobKiller() {
        super("MobKiller", "Kills passive mobs in range for their drops", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        double r = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!matchesType(entity)) continue;
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (animal.isBaby()) continue;
            if (mc.player.distanceTo(entity) > r) continue;

            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }

    private boolean matchesType(Entity entity) {
        return switch (type.get()) {
            case "Cow"     -> entity instanceof CowEntity;
            case "Chicken" -> entity instanceof ChickenEntity;
            case "Pig"     -> entity instanceof PigEntity;
            default        -> entity instanceof CowEntity || entity instanceof ChickenEntity || entity instanceof PigEntity;
        };
    }
}
