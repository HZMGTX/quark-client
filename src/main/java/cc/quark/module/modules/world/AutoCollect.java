package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * AutoCollect - automatically moves toward and collects nearby item entities.
 *
 * Scans the world for ItemEntity objects within range and either teleports the
 * player toward them or adds velocity to pull items into the collection radius.
 * Uses a configurable range and delay to avoid being too aggressive.
 */
public class AutoCollect extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect and collect dropped items", 6.0, 1.0, 20.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between collection attempts", 200, 50, 2000));
    private final BoolSetting moveToItem = register(new BoolSetting(
            "Move To Item", "Move the player toward items outside pickup range", true));
    private final DoubleSetting moveSpeed = register(new DoubleSetting(
            "Move Speed", "Speed to move toward items", 0.3, 0.05, 2.0));
    private final BoolSetting pullItems = register(new BoolSetting(
            "Pull Items", "Add velocity to nearby items to pull them toward the player", true));
    private final DoubleSetting pullSpeed = register(new DoubleSetting(
            "Pull Speed", "Velocity added to items each tick to pull them in", 0.25, 0.05, 1.5));
    private final BoolSetting ignoreFullInv = register(new BoolSetting(
            "Ignore Full Inv", "Stop collecting when inventory is full", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoCollect() {
        super("AutoCollect", "Automatically collects nearby dropped items into inventory", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Check if inventory is full
        if (ignoreFullInv.isEnabled() && isInventoryFull()) return;

        ItemEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity item)) continue;
            double dist = mc.player.distanceTo(item);
            if (dist > range.get()) continue;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = item;
            }
        }

        if (nearest == null) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d itemPos   = nearest.getPos();

        // Pull items toward the player with velocity
        if (pullItems.isEnabled()) {
            Vec3d dir = playerPos.subtract(itemPos).normalize().multiply(pullSpeed.get());
            nearest.setVelocity(dir.x, dir.y + 0.1, dir.z);
        }

        // Move player toward item if it's not within natural pickup range (~1.5 blocks)
        if (moveToItem.isEnabled() && nearestDist > 1.5) {
            Vec3d dir = itemPos.subtract(playerPos).normalize().multiply(moveSpeed.get());
            mc.player.setVelocity(dir.x, mc.player.getVelocity().y, dir.z);
        }

        timer.reset();
    }

    private boolean isInventoryFull() {
        if (mc.player == null) return false;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return false;
        }
        return true;
    }
}
