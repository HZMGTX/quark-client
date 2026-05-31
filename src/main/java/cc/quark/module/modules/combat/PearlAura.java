package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class PearlAura extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Distance threshold to throw pearl at enemy (blocks)", 12, 5, 20));

    private final TimerUtil timer = new TimerUtil();

    public PearlAura() {
        super("PearlAura", "Throws ender pearls at enemies to teleport into melee range", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(3000)) return;

        PlayerEntity target = findNearestEnemy(range.get());
        if (target == null) return;

        double dist = mc.player.distanceTo(target);
        if (dist < 5) return;

        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = pearlSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));

        aimAt(target);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        timer.reset();
    }

    private void aimAt(PlayerEntity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private PlayerEntity findNearestEnemy(int maxRange) {
        PlayerEntity nearest = null;
        double best = maxRange;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player || p.isDead() || p.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) return i;
        }
        return -1;
    }
}
