package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AutoButcher - automatically kills animals when their population exceeds a
 * configurable limit, helping manage farm entities and prevent lag.
 *
 * The module counts nearby animals of each species. If the count exceeds
 * the configured maximum, it attacks the excess animals (prioritising adults
 * over babies). A configurable delay prevents packet spam.
 */
public class AutoButcher extends Module {

    private final IntSetting maxAnimals = register(new IntSetting(
            "Max Animals", "Kill animals until this many remain per species", 10, 1, 100));
    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for animals", 5.0, 2.0, 16.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between each kill", 250, 50, 2000));
    private final BoolSetting keepBabies = register(new BoolSetting(
            "Keep Babies", "Never kill baby animals", true));
    private final BoolSetting onlyAdults = register(new BoolSetting(
            "Only Adults", "Only count and kill adult animals (ignore babies in count)", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoButcher() {
        super("AutoButcher", "Kills excess animals above a configurable per-species count", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        List<AnimalEntity> candidates = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (mc.player.distanceTo(animal) > range.get()) continue;
            if (keepBabies.isEnabled() && animal.isBaby()) continue;
            if (onlyAdults.isEnabled() && animal.isBaby()) continue;
            candidates.add(animal);
        }

        // Group by class (species)
        java.util.Map<Class<?>, List<AnimalEntity>> bySpecies = new java.util.HashMap<>();
        for (AnimalEntity animal : candidates) {
            bySpecies.computeIfAbsent(animal.getClass(), k -> new ArrayList<>()).add(animal);
        }

        int max = maxAnimals.get();

        for (List<AnimalEntity> group : bySpecies.values()) {
            if (group.size() <= max) continue;

            // Sort: adults first (to kill adults before babies if keepBabies is off)
            group.sort(Comparator.comparingInt(a -> a.isBaby() ? 1 : 0));

            // Kill the excess
            int toKill = group.size() - max;
            for (int i = 0; i < toKill; i++) {
                mc.interactionManager.attackEntity(mc.player, group.get(i));
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                timer.reset();
                return; // one per tick cycle
            }
        }
    }
}
