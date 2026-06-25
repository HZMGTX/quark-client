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
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class FlameAura extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Range to target enemies (blocks)", 5, 2, 10));

    private final TimerUtil timer = new TimerUtil();

    public FlameAura() {
        super("FlameAura", "Shoots flame arrows or uses flint and steel to ignite nearby enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;

        PlayerEntity target = findNearestEnemy(range.get());
        if (target == null) return;

        int flintSlot = findFlintAndSteel();
        if (flintSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = flintSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(flintSlot));

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.interact(target, mc.player.isSneaking(), Hand.MAIN_HAND));
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        timer.reset();
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

    private int findFlintAndSteel() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.FLINT_AND_STEEL)) return i;
        }
        return -1;
    }
}
