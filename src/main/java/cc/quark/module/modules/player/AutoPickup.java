package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;

public class AutoPickup extends Module {

    private final DoubleSetting range  = register(new DoubleSetting("Range",  "Distance within which items are auto-picked up", 5.0, 1.0, 15.0));
    private final DoubleSetting speed  = register(new DoubleSetting("Speed",  "Movement speed toward item per tick", 0.25, 0.05, 1.0));
    private final BoolSetting onGround = register(new BoolSetting("Ground Only", "Only move toward items when on the ground", true));

    public AutoPickup() {
        super("AutoPickup", "Automatically moves toward nearby ItemEntity drops to collect them", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (onGround.isEnabled() && !mc.player.isOnGround()) return;

        ItemEntity nearest = null;
        double nearestDist = range.get() + 1;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity ie)) continue;
            double dist = mc.player.distanceTo(ie);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = ie;
            }
        }

        if (nearest == null || nearestDist > range.get()) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d itemPos   = nearest.getPos();
        Vec3d diff      = itemPos.subtract(playerPos);

        if (diff.lengthSquared() < 0.1) return;

        Vec3d dir = diff.normalize().multiply(speed.get());
        mc.player.setVelocity(mc.player.getVelocity().add(dir.x, 0, dir.z));
    }

    @Override
    public String getSuffix() {
        if (mc.world == null || mc.player == null) return "";
        int count = 0;
        for (var e : mc.world.getEntities()) {
            if (e instanceof ItemEntity && mc.player.distanceTo(e) <= range.get()) count++;
        }
        return count > 0 ? count + " items" : "";
    }
}
