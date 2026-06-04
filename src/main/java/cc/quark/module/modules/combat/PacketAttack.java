package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class PacketAttack extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 1.0, 6.0));

    private final IntSetting cps = register(new IntSetting(
            "CPS", "Clicks per second", 12, 1, 20));

    private final TimerUtil timer = new TimerUtil();

    public PacketAttack() {
        super("PacketAttack", "Sends attack packets without animation", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long delay = 1000L / Math.max(1, cps.get());
        if (!timer.hasReached(delay)) return;

        LivingEntity closest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < minDist) {
                minDist = dist;
                closest = living;
            }
        }

        if (closest != null) {
            // Send attack packet directly without triggering swing animation
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        PlayerInteractEntityC2SPacket.attack(closest, mc.player.isSneaking()));
            }
            timer.reset();
        }
    }
}
