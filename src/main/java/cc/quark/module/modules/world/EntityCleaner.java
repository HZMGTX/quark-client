package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class EntityCleaner extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius for entities to clean", 5, 1, 15));
    private final BoolSetting itemEntities = register(new BoolSetting(
            "Items", "Clean up item entities", true));
    private final BoolSetting expOrbs = register(new BoolSetting(
            "ExpOrbs", "Clean up experience orbs", true));
    private final BoolSetting armorStands = register(new BoolSetting(
            "ArmorStands", "Clean up armor stands", false));
    private final BoolSetting keepValuable = register(new BoolSetting(
            "KeepValuable", "Skip valuable item entities", true));

    private final TimerUtil timer = new TimerUtil();

    public EntityCleaner() {
        super("EntityCleaner", "Removes nearby item entities, XP orbs, or armor stands", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        double r = range.get();
        Box searchBox = mc.player.getBoundingBox().expand(r);

        List<net.minecraft.entity.Entity> targets = new ArrayList<>();

        if (itemEntities.isEnabled()) {
            mc.world.getEntitiesByClass(ItemEntity.class, searchBox, e -> {
                if (keepValuable.isEnabled() && isValuable(e.getStack().getItem())) return false;
                return e.distanceTo(mc.player) <= r;
            }).forEach(targets::add);
        }

        if (expOrbs.isEnabled()) {
            mc.world.getEntitiesByClass(ExperienceOrbEntity.class, searchBox,
                    e -> e.distanceTo(mc.player) <= r).forEach(targets::add);
        }

        if (armorStands.isEnabled()) {
            mc.world.getEntitiesByClass(ArmorStandEntity.class, searchBox,
                    e -> e.distanceTo(mc.player) <= r).forEach(targets::add);
        }

        if (targets.isEmpty()) return;

        var target = targets.stream()
                .min((a, b) -> Double.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
                .orElse(null);

        if (target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean isValuable(Item item) {
        return item == Items.DIAMOND || item == Items.EMERALD
                || item == Items.NETHERITE_INGOT || item == Items.NETHERITE_SCRAP
                || item == Items.ANCIENT_DEBRIS || item == Items.GOLD_INGOT
                || item instanceof SwordItem || item instanceof PickaxeItem
                || item instanceof ArmorItem;
    }
}
