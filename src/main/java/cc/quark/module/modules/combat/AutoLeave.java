package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.text.Text;

public class AutoLeave extends Module {

    private final DoubleSetting health = register(new DoubleSetting(
            "Health", "Health threshold to leave at", 6.0, 1.0, 20.0));

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "What to do when health is low", "Disconnect", "Disconnect", "Pause", "Respawn"));

    private final BoolSetting instant = register(new BoolSetting(
            "Instant", "Act immediately without delay", true));

    private boolean triggered = false;

    public AutoLeave() {
        super("AutoLeave", "Disconnects or pauses when health drops below threshold", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        triggered = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.getHealth() > health.get()) {
            triggered = false;
            return;
        }
        if (triggered && !instant.isEnabled()) return;
        triggered = true;

        switch (mode.get()) {
            case "Disconnect" -> {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().getConnection().disconnect(Text.literal("AutoLeave"));
                }
            }
            case "Pause" -> mc.setScreen(new net.minecraft.client.gui.screen.GameMenuScreen(true));
            case "Respawn" -> {
                if (mc.player.isRemoved() && mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket(
                            net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                }
            }
        }
    }
}
