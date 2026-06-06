package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * MobStacker — teleports nearby mobs into a single location for efficient farming.
 * Stacks mobs by setting their position to the player's position (client-side nudge).
 */
public class MobStacker extends Module {

    private final DoubleSetting radius = register(new DoubleSetting(
            "Radius", "Radius to search for mobs", 10.0, 2.0, 20.0));
    private final IntSetting maxMobs = register(new IntSetting(
            "Max Mobs", "Maximum number of mobs to stack per cycle", 20, 1, 100));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between stack cycles", 1000, 200, 5000));
    private final BoolSetting stackHostile = register(new BoolSetting(
            "Hostile", "Stack hostile mobs", true));
    private final BoolSetting stackPassive = register(new BoolSetting(
            "Passive", "Stack passive mobs", false));

    private final TimerUtil timer = new TimerUtil();

    public MobStacker() {
        super("MobStacker", "Stacks nearby mobs into one location for efficient farming", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(delay.get())) return;

        double r = radius.get();
        Vec3d target = mc.player.getPos();
        Box box = mc.player.getBoundingBox().expand(r);

        List<net.minecraft.entity.Entity> entities = mc.world.getOtherEntities(
                mc.player, box, e -> {
                    if (stackHostile.isEnabled() && e instanceof HostileEntity) return true;
                    if (stackPassive.isEnabled()  && e instanceof PassiveEntity)  return true;
                    return false;
                });

        int count = 0;
        for (var entity : entities) {
            if (count >= maxMobs.get()) break;
            if (entity.getPos().distanceTo(target) < 1.5) continue;

            // Move the entity toward the stack center (client-side)
            entity.setPosition(target.x, target.y, target.z);
            entity.setVelocity(Vec3d.ZERO);
            count++;
        }

        if (count > 0) timer.reset();
    }
}
