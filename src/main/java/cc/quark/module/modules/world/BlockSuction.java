package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class BlockSuction extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to attract nearby dropped item entities", 3.0, 1.0, 8.0));

    public BlockSuction() {
        super("BlockSuction", "Attracts nearby dropped block/item entities toward the player", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        double rangeSq = range.get() * range.get();
        Vec3d playerPos = mc.player.getPos();

        ItemEntity nearest = null;
        double nearestDist = rangeSq;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity item)) continue;
            double distSq = mc.player.squaredDistanceTo(item);
            if (distSq < nearestDist) {
                nearestDist = distSq;
                nearest = item;
            }
        }

        if (nearest == null) return;

        // Send a small nudge packet toward the item to attract it
        Vec3d itemPos = nearest.getPos();
        Vec3d dir = itemPos.subtract(playerPos).normalize().multiply(0.15);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x + dir.x,
                playerPos.y,
                playerPos.z + dir.z,
                mc.player.isOnGround()));
    }
}
