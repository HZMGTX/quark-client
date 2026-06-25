package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;

public class AutoLoot extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to automatically pick up dropped items", 5.0, 1.0, 10.0));

    public AutoLoot() {
        super("AutoLoot", "Auto-picks up loot after killing enemies", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double r = range.get();
        ItemEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity item)) continue;
            double dist = mc.player.distanceTo(item);
            if (dist <= r && dist < closestDist) {
                closestDist = dist;
                closest = item;
            }
        }

        if (closest != null) {
            Vec3d dir = closest.getPos().subtract(mc.player.getPos()).normalize();
            mc.player.setVelocity(
                    mc.player.getVelocity().add(dir.x * 0.15, 0, dir.z * 0.15));
        }
    }
}
