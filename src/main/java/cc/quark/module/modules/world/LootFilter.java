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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * LootFilter - Only moves toward (picks up) items matching the whitelist.
 * Invert mode makes it pick up everything EXCEPT the listed items.
 */
public class LootFilter extends Module {

    private final StringSetting whitelist = register(new StringSetting(
            "Whitelist", "Comma-separated item names to allow", "diamond,emerald"));
    private final BoolSetting invert = register(new BoolSetting(
            "Invert", "Pick up everything EXCEPT listed items", false));

    public LootFilter() {
        super("LootFilter", "Only picks up items matching filter list", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Set<String> names = buildSet();

        ItemEntity nearest = null;
        double closestDist = 6.0;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity itemEnt)) continue;

            String itemName = itemEnt.getStack().getName().getString().toLowerCase();
            boolean inList = names.stream().anyMatch(itemName::contains);

            boolean shouldPickup = invert.isEnabled() ? !inList : inList;
            if (!shouldPickup) continue;

            double dist = mc.player.distanceTo(itemEnt);
            if (dist < closestDist) {
                closestDist = dist;
                nearest = itemEnt;
            }
        }

        if (nearest == null || closestDist < 0.5) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d dir = nearest.getPos().subtract(playerPos).normalize().multiply(0.2);
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x + dir.x,
                playerPos.y,
                playerPos.z + dir.z,
                mc.player.isOnGround()));
    }

    private Set<String> buildSet() {
        Set<String> set = new HashSet<>();
        for (String s : whitelist.get().split(",")) {
            String t = s.trim().toLowerCase();
            if (!t.isEmpty()) set.add(t);
        }
        return set;
    }
}
