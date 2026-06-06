package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AnimalBreeder extends Module {
    private final IntSetting range = register(new IntSetting("Range", "Breeding range", 5, 1, 10));
    private final TimerUtil timer = new TimerUtil();

    public AnimalBreeder() { super("AnimalBreeder", "Auto-breeds nearby animals with appropriate food", Category.WORLD); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(300)) return;
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof AnimalEntity animal)) continue;
            if (mc.player.distanceTo(animal) > range.get()) continue;
            if (animal.isInLove() || animal.isBaby()) continue;
            mc.interactionManager.interactEntity(mc.player, animal, Hand.MAIN_HAND);
            timer.reset();
            return;
        }
    }
}
