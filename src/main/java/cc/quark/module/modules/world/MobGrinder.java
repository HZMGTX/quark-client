package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

public class MobGrinder extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect mobs and push toward grinder", 8.0, 2.0, 16.0));

    private final TimerUtil timer = new TimerUtil();

    public MobGrinder() {
        super("MobGrinder", "Optimizes mob grinder efficiency by attacking mobs to push them toward the kill zone", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(150)) return;
        timer.reset();

        double rangeSq = range.get() * range.get();

        // Find all mobs in range and attack them to trigger knockback toward the center
        List<MobEntity> mobs = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof MobEntity)
                .map(e -> (MobEntity) e)
                .filter(m -> mc.player.squaredDistanceTo(m) <= rangeSq)
                .sorted(Comparator.comparingDouble(m -> mc.player.squaredDistanceTo(m)))
                .toList();

        if (mobs.isEmpty()) return;

        // Attack closest mob — velocity from the hit will push it
        MobEntity target = mobs.get(0);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Apply a slight push toward player to funnel into grinder
        Vec3d toPlayer = mc.player.getPos().subtract(target.getPos()).normalize().multiply(0.3);
        target.addVelocity(toPlayer.x, 0.1, toPlayer.z);
    }
}
