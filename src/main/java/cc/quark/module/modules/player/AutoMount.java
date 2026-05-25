package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Hand;

public class AutoMount extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Mount entities within this distance", 3.0, 1.0, 6.0));

    public AutoMount() {
        super("AutoMount", "Automatically mounts nearby rideable entities", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getVehicle() != null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!isRideable(entity)) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);
            return;
        }
    }

    private boolean isRideable(Entity entity) {
        return entity instanceof AbstractHorseEntity
            || entity instanceof PigEntity
            || entity instanceof StriderEntity
            || entity instanceof BoatEntity
            || entity instanceof AbstractMinecartEntity;
    }
}
