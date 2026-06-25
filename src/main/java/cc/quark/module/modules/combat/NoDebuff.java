package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;

public class NoDebuff extends Module {

    private final BoolSetting onlyCombat = register(new BoolSetting(
            "OnlyCombat", "Only cancel debuffs received during combat (near players)", true));

    public NoDebuff() {
        super("NoDebuff", "Cancels mining fatigue, slowness, blindness effects from server", Category.COMBAT);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityStatusEffectS2CPacket pkt)) return;
        if (pkt.getEntityId() != mc.player.getId()) return;

        var effectId = pkt.getEffectId();
        boolean isDebuff = effectId.equals(StatusEffects.MINING_FATIGUE)
                || effectId.equals(StatusEffects.SLOWNESS)
                || effectId.equals(StatusEffects.BLINDNESS)
                || effectId.equals(StatusEffects.DARKNESS);

        if (!isDebuff) return;

        if (onlyCombat.isEnabled() && !nearPlayer()) return;

        event.setCancelled(true);
    }

    private boolean nearPlayer() {
        if (mc.world == null) return false;
        for (var entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof net.minecraft.entity.player.PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) <= 16.0) return true;
        }
        return false;
    }
}
