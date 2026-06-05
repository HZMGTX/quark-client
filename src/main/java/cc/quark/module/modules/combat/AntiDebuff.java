package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;

public class AntiDebuff extends Module {
    private final BoolSetting slowness = register(new BoolSetting("Slowness", "Cancel slowness", true));
    private final BoolSetting weakness = register(new BoolSetting("Weakness", "Cancel weakness", true));
    private final BoolSetting blindness = register(new BoolSetting("Blindness", "Cancel blindness", true));
    private final BoolSetting nausea = register(new BoolSetting("Nausea", "Cancel nausea", true));
    private final BoolSetting wither = register(new BoolSetting("Wither", "Cancel wither", false));

    public AntiDebuff() {
        super("Anti Debuff", "Cancel negative status effects from packets", Category.COMBAT, 0);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof EntityStatusEffectS2CPacket pkt)) return;
        if (mc.player == null || pkt.getEntityId() != mc.player.getId()) return;

        // Get effect type via registry
        var effectOpt = net.minecraft.registry.Registries.STATUS_EFFECT.getEntry(pkt.getEffectId());
        if (effectOpt.isEmpty()) return;
        var effect = effectOpt.get();

        if ((slowness.isEnabled() && effect.matches(StatusEffects.SLOWNESS)) ||
            (weakness.isEnabled() && effect.matches(StatusEffects.WEAKNESS)) ||
            (blindness.isEnabled() && effect.matches(StatusEffects.BLINDNESS)) ||
            (nausea.isEnabled() && effect.matches(StatusEffects.NAUSEA)) ||
            (wither.isEnabled() && effect.matches(StatusEffects.WITHER))) {
            event.cancel();
        }
    }
}
