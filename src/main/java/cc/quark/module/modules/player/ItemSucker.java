package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ItemSucker extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to suck in items", 5, 1, 15));
    private final DoubleSetting force = register(new DoubleSetting(
            "Force", "Velocity force applied toward player", 0.3, 0.05, 1.0));

    public ItemSucker() {
        super("ItemSucker", "Pulls nearby item entities toward the player", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        int r = range.get();
        Box searchBox = mc.player.getBoundingBox().expand(r);
        List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class, searchBox,
                e -> e.distanceTo(mc.player) <= r);

        Vec3d playerPos = mc.player.getPos();
        double f = force.get();

        for (ItemEntity item : items) {
            Vec3d dir = playerPos.subtract(item.getPos()).normalize();
            item.setVelocity(item.getVelocity().add(dir.multiply(f)));
        }
    }
}
