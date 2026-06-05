package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;

public class AntiPotion2 extends Module {
    private final BoolSetting blockSlowness = register(new BoolSetting("Slowness", "Block slowness", true));
    private final BoolSetting blockWeakness = register(new BoolSetting("Weakness", "Block weakness", true));
    private final BoolSetting blockPoison = register(new BoolSetting("Poison", "Block poison", true));
    private final BoolSetting blockBlindness = register(new BoolSetting("Blindness", "Block blindness", true));

    public AntiPotion2() { super("AntiPotion2", "Blocks harmful status effects from being applied", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (!(e.getPacket() instanceof EntityStatusEffectS2CPacket pkt)) return;
        if (mc.player == null || pkt.getEntityId() != mc.player.getId()) return;
        int id = pkt.getEffectId();
        var reg = mc.player.getWorld().getRegistryManager().get(net.minecraft.registry.RegistryKeys.STATUS_EFFECT);
        var entry = reg.getEntry(id);
        if (entry.isEmpty()) return;
        var effect = entry.get().value();
        if (blockSlowness.isEnabled() && effect == StatusEffects.SLOWNESS.value()) e.cancel();
        if (blockWeakness.isEnabled() && effect == StatusEffects.WEAKNESS.value()) e.cancel();
        if (blockPoison.isEnabled() && effect == StatusEffects.POISON.value()) e.cancel();
        if (blockBlindness.isEnabled() && effect == StatusEffects.BLINDNESS.value()) e.cancel();
    }
}
