package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ExpOrb extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to attract XP orbs toward the player", 5.0, 1.0, 12.0));

    public ExpOrb() {
        super("ExpOrb", "Attracts nearby XP orbs to the player by moving toward them", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        double rangeSq = range.get() * range.get();
        Vec3d playerPos = mc.player.getPos();

        ExperienceOrbEntity nearest = null;
        double nearestDist = rangeSq;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ExperienceOrbEntity orb)) continue;
            double distSq = mc.player.squaredDistanceTo(orb);
            if (distSq < nearestDist) {
                nearestDist = distSq;
                nearest = orb;
            }
        }

        if (nearest == null) return;

        // Move a packet step toward the XP orb
        Vec3d orbPos = nearest.getPos();
        Vec3d dir = orbPos.subtract(playerPos).normalize().multiply(0.2);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x + dir.x,
                playerPos.y,
                playerPos.z + dir.z,
                mc.player.isOnGround()));
    }
}
