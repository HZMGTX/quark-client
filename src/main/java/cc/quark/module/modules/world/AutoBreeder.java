package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

public class AutoBreeder extends Module {

    private final DoubleSetting range    = register(new DoubleSetting("Range",   "Breeding range",               3.0, 1.0, 6.0));
    private final DoubleSetting interval = register(new DoubleSetting("Interval","Seconds between breed attempts", 1.0, 0.2, 5.0));

    private long lastBreed = 0;

    public AutoBreeder() {
        super("AutoBreeder", "Automatically feeds and breeds nearby animals using your held food item", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        long now = System.currentTimeMillis();
        if (now - lastBreed < (long)(interval.get() * 1000)) return;

        var held = mc.player.getMainHandStack();
        if (held.isEmpty()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (mc.player.distanceTo(animal) > range.get()) continue;
            if (!animal.isBreedingItem(held)) continue;
            if (animal.getLoveTicks() > 0) continue;

            mc.interactionManager.interactEntity(mc.player, animal, Hand.MAIN_HAND);
            lastBreed = now;
            return;
        }
    }
}
