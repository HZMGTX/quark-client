package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.registry.Registries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EntityCleaner2 extends Module {

    private final StringSetting targets = register(new StringSetting(
            "Targets", "Comma-separated entity types to remove", "item,xp_orb"));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to clean entities", 16.0, 4.0, 64.0));

    public EntityCleaner2() {
        super("EntityCleaner2", "Removes entity lag (kills specific types)", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Set<String> targetSet = new HashSet<>(
                Arrays.asList(targets.get().toLowerCase().split(",")));

        double r = range.get();
        mc.world.getEntities().forEach(entity -> {
            if (mc.player.distanceTo(entity) > r) return;
            boolean shouldRemove = false;
            if (targetSet.contains("item") && entity instanceof ItemEntity) shouldRemove = true;
            if (targetSet.contains("xp_orb") && entity instanceof ExperienceOrbEntity) shouldRemove = true;
            if (shouldRemove) entity.discard();
        });
    }
}
