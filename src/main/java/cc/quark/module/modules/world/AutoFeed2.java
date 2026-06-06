package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Optional;

public class AutoFeed2 extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Feed range", 4.0, 1.0, 8.0));
    private final BoolSetting bredOnly = register(new BoolSetting("BreedOnly", "Only feed breedable animals", true));
    private int delay = 0;
    public AutoFeed2() { super("AutoFeed2", "Automatically feeds nearby animals for breeding", Category.WORLD); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || ++delay < 10) return;
        delay = 0;
        Optional<AnimalEntity> nearest = mc.world.getEntitiesByClass(AnimalEntity.class,
            mc.player.getBoundingBox().expand(range.getValue()), e -> e.isBreedingItem(mc.player.getMainHandStack())).stream()
            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
        nearest.ifPresent(e -> mc.interactionManager.interactEntity(mc.player, e, Hand.MAIN_HAND));
    }
}
