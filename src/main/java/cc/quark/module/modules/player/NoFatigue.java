package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class NoFatigue extends Module {

    private static final int MINING_FATIGUE_STATUS = 28;

    private final BoolSetting alsoWeakness = register(new BoolSetting("Also Weakness", "Also remove weakness effect", false));

    public NoFatigue() {
        super("NoFatigue", "Removes mining fatigue", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof EntityStatusS2CPacket pkt)) return;
        if (pkt.getStatus() == MINING_FATIGUE_STATUS) {
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
        if (alsoWeakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
        }
    }
}
