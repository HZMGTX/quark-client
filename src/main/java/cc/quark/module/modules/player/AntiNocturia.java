package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class AntiNocturia extends Module {

    private final BoolSetting autoWalk = register(new BoolSetting(
            "AutoWalk", "Walk forward after refusing bed to avoid AFK kick", false));

    private boolean shouldWalk = false;

    public AntiNocturia() {
        super("AntiNocturia", "Prevents server from forcing player position to bed during night", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        shouldWalk = false;
        if (mc.player != null) mc.options.forwardKey.setPressed(false);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof GameStateChangeS2CPacket pkt)) return;
        mc.execute(() -> {
            if (mc.player == null) return;
            if (mc.player.isSleeping()) {
                mc.player.wakeUp();
                shouldWalk = true;
            }
        });
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (autoWalk.isEnabled() && shouldWalk) {
            mc.options.forwardKey.setPressed(true);
        }
    }
}
