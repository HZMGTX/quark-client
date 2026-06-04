package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * ItemAura - Attracts nearby dropped items toward the player by sending position packets.
 */
public class ItemAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Item attraction range in blocks", 3.0, 1.0, 8.0));

    public ItemAura() {
        super("ItemAura", "Auto-picks up items within range", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double r = range.get();
        Vec3d playerPos = mc.player.getPos();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity item)) continue;
            double dist = item.distanceTo(mc.player);
            if (dist > r) continue;
            if (dist < 0.5) continue; // Close enough already

            // Move player slightly toward the item
            Vec3d toItem = item.getPos().subtract(playerPos).normalize().multiply(0.15);
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    playerPos.x + toItem.x,
                    playerPos.y,
                    playerPos.z + toItem.z,
                    mc.player.isOnGround()));
            break; // One item per tick
        }
    }
}
