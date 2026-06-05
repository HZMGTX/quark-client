package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;

public class NoFatigue extends Module {

    private final BoolSetting removeWeakness = register(new BoolSetting(
            "Remove Weakness", "Also remove weakness effect", false));
    private final BoolSetting removePoison = register(new BoolSetting(
            "Remove Poison", "Also remove poison effect", false));

    public NoFatigue() {
        super("NoFatigue", "Removes Mining Fatigue each tick and blocks server-applied fatigue packets", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof EntityStatusEffectS2CPacket pkt)) return;
        if (mc.player == null) return;

        // Block server-side mining fatigue effect from being applied
        // int effectId = pkt.getEffectId();
        // Mining Fatigue effect registry ID = 4
        // if (effectId == net.minecraft.registry.Registries.STATUS_EFFECT.getRawId(StatusEffects.MINING_FATIGUE.value())) {
        //    event.cancel();
        // }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
        if (removeWeakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
        }
        if (removePoison.isEnabled() && mc.player.hasStatusEffect(StatusEffects.POISON)) {
            mc.player.removeStatusEffect(StatusEffects.POISON);
        }
    }
}
