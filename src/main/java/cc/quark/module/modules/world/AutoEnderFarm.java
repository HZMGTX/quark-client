package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Optional;

public class AutoEnderFarm extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Look range for endermen", 6.0, 3.0, 12.0));
    private final IntSetting lookDelay = register(new IntSetting("LookDelay", "Ticks between looks", 3, 1, 20));
    private final BoolSetting killMode = register(new BoolSetting("KillMode", "Attack endermen after aggro", true));

    private int tickTimer = 0;

    public AutoEnderFarm() {
        super("AutoEnderFarm", "Automates enderman farming", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (++tickTimer < lookDelay.getValue()) return;
        tickTimer = 0;

        Optional<EndermanEntity> nearest = mc.world.getEntitiesByClass(EndermanEntity.class,
                mc.player.getBoundingBox().expand(range.getValue()), e -> true)
            .stream()
            .filter(e -> !e.isAngry())
            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));

        if (nearest.isEmpty()) return;
        EndermanEntity target = nearest.get();
        Vec3d pos = target.getEyePos();
        Vec3d diff = pos.subtract(mc.player.getEyePos());
        double yaw = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }
}
