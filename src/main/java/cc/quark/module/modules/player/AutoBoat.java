package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Hand;

/**
 * AutoBoat — automatically enters a nearby boat when the player is not
 * already riding a vehicle.
 */
public class AutoBoat extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search range for nearby boats (blocks)", 4.0, 1.0, 10.0));

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Notify in chat when entering a boat", false));

    private final BoolSetting onlyEmpty = register(new BoolSetting(
            "Only Empty", "Only enter boats without a passenger", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBoat() {
        super("AutoBoat", "Auto-enters nearby boats", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Already in a vehicle — nothing to do
        if (mc.player.getVehicle() != null) return;

        // Throttle to every 500 ms
        if (!timer.hasReached(500)) return;
        timer.reset();

        BoatEntity closestBoat = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof BoatEntity boat)) continue;

            // Check whether it already has a passenger
            if (onlyEmpty.isEnabled() && !boat.getPassengerList().isEmpty()) continue;

            double dist = mc.player.distanceTo(boat);
            if (dist > range.get()) continue;

            if (dist < closestDist) {
                closestDist = dist;
                closestBoat = boat;
            }
        }

        if (closestBoat == null) return;

        mc.interactionManager.interactEntity(mc.player, closestBoat, Hand.MAIN_HAND);

        if (notify.isEnabled()) {
            ChatUtil.info("AutoBoat: entered boat at "
                    + String.format("%.0f, %.0f, %.0f",
                            closestBoat.getX(), closestBoat.getY(), closestBoat.getZ()));
        }
    }
}
