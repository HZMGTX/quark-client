package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class AutoPickupFilter extends Module {

    private final StringSetting filter = register(new StringSetting(
            "Filter", "Comma-separated item names to pick up (empty = all)", ""));
    private final BoolSetting allItems = register(new BoolSetting(
            "AllItems", "Pick up all items regardless of filter", true));

    public AutoPickupFilter() {
        super("AutoPickupFilter", "Auto-picks up items matching a configurable filter", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Set<String> names = getFilterSet();

        ItemEntity nearest = null;
        double closestDist = 6.0;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity itemEnt)) continue;
            String itemName = itemEnt.getStack().getName().getString().toLowerCase();

            if (!allItems.isEnabled() && !names.isEmpty()) {
                if (names.stream().noneMatch(itemName::contains)) continue;
            }

            double dist = mc.player.distanceTo(itemEnt);
            if (dist < closestDist) {
                closestDist = dist;
                nearest = itemEnt;
            }
        }

        if (nearest == null) return;

        Vec3d itemPos = nearest.getPos();
        Vec3d playerPos = mc.player.getPos();
        Vec3d dir = itemPos.subtract(playerPos).normalize().multiply(0.2);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x + dir.x, playerPos.y, playerPos.z + dir.z, mc.player.isOnGround()));
    }

    private Set<String> getFilterSet() {
        Set<String> set = new HashSet<>();
        for (String s : filter.get().split(",")) {
            String t = s.trim().toLowerCase();
            if (!t.isEmpty()) set.add(t);
        }
        return set;
    }
}
