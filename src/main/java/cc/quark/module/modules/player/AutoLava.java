package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class AutoLava extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to target enemies for lava placement", 4.0, 2.0, 8.0));
    private final DoubleSetting interval = register(new DoubleSetting(
            "Interval", "Seconds between lava placements", 3.0, 0.5, 10.0));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoLava() {
        super("AutoLava", "Switches to lava bucket and places at enemy feet when enemies are nearby", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (prevSlot >= 0 && prevSlot < 9 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached((long) (interval.get() * 1000.0))) return;

        int slot = InventoryUtil.findItem(Items.LAVA_BUCKET);
        if (slot < 0 || slot >= 9) return;

        // Find nearest enemy in range
        LivingEntity target = null;
        double best = range.get();
        for (var entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity le) || le.isRemoved()) continue;
            double dist = mc.player.distanceTo(le);
            if (dist < best) { best = dist; target = le; }
        }
        if (target == null) return;

        // Look at target's feet
        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();
        double dy = target.getY() - mc.player.getEyeY();
        float yaw   = (float) Math.toDegrees(MathHelper.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-MathHelper.atan2(dy,
                Math.sqrt(dx * dx + dz * dz)));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);

        if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }
}
