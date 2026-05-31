package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class InstantPop extends Module {

    private final TimerUtil timer = new TimerUtil();

    public InstantPop() {
        super("InstantPop", "Detects totem pops on nearby players and immediately attacks", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(event.getPacket() instanceof EntityStatusS2CPacket pkt)) return;
        // Status 35 = totem of undying pop
        if (pkt.getStatus() != 35) return;

        mc.execute(() -> {
            if (!timer.hasReached(500)) return;
            var entity = pkt.getEntity(mc.world);
            if (!(entity instanceof PlayerEntity popped)) return;
            if (popped == mc.player) return;
            if (EntityUtil.isFriend(popped)) return;
            if (EntityUtil.distanceTo(popped) > 6) return;

            mc.interactionManager.attackEntity(mc.player, popped);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
        });
    }
}
