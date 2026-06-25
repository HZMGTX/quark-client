package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * AutoSteal - automatically moves toward and picks up item drops from
 * recently killed enemies. Searches for item entities near dead player
 * positions and sucks them into the player's inventory.
 */
public class AutoSteal extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius around player to collect drops", 6.0, 1.0, 12.0));

    private final BoolSetting magnetMode = register(new BoolSetting(
            "Magnet", "Pull items toward player rather than teleporting pickup", true));

    private final DoubleSetting magnetSpeed = register(new DoubleSetting(
            "Magnet Speed", "Velocity applied to items when Magnet is on", 0.5, 0.1, 2.0));

    public AutoSteal() {
        super("AutoSteal", "Auto-collects item drops from killed enemies", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double r = range.get();
        Box searchBox = mc.player.getBoundingBox().expand(r);
        List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class, searchBox, e -> !e.isRemoved());

        if (items.isEmpty()) return;

        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);

        for (ItemEntity item : items) {
            double dist = item.distanceTo(mc.player);
            if (dist > r) continue;

            if (magnetMode.isEnabled()) {
                // Push item velocity toward the player
                Vec3d itemPos = item.getPos();
                Vec3d dir = playerPos.subtract(itemPos).normalize();
                double spd = magnetSpeed.get();
                item.setVelocity(dir.x * spd, dir.y * spd + 0.1, dir.z * spd);
            }
        }
    }
}
