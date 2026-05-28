package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.util.math.BlockPos;

public class AutoSign extends Module {

    private final ModeSetting preset = register(new ModeSetting(
            "Preset", "Text preset to write on signs",
            "Quark.cc",
            "Quark.cc", "Hello!", "GG", "Come at me", "For Sale", "Private", "No Entry", "Blank"));

    private final BoolSetting closeAfter = register(new BoolSetting(
            "Auto Close", "Close the sign editor after filling", true));

    private BlockPos pendingPos = null;
    private boolean  isFront   = true;

    public AutoSign() {
        super("AutoSign", "Automatically fills signs with preset text when the sign editor opens", Category.WORLD);
    }

    @Override
    public void onEnable() {
        pendingPos = null;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (event.getPacket() instanceof SignEditorOpenS2CPacket pkt) {
            pendingPos = pkt.getPos();
            isFront    = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!(mc.currentScreen instanceof AbstractSignEditScreen)) return;
        if (pendingPos == null || mc.getNetworkHandler() == null) return;

        String[] lines = buildLines();
        mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(
                pendingPos, isFront,
                lines[0], lines[1], lines[2], lines[3]));
        pendingPos = null;

        if (closeAfter.isEnabled()) mc.setScreen(null);
    }

    private String[] buildLines() {
        return switch (preset.get()) {
            case "Hello!"     -> new String[]{"Hello!", "", "", ""};
            case "GG"         -> new String[]{"GG", "EZ", "", ""};
            case "Come at me" -> new String[]{"Come at", "me bro", "", ""};
            case "For Sale"   -> new String[]{"For Sale", "Contact me", "in chat", ""};
            case "Private"    -> new String[]{"PRIVATE", "Keep Out", "", ""};
            case "No Entry"   -> new String[]{"No Entry", "Authorized", "Personnel Only", ""};
            case "Blank"      -> new String[]{"", "", "", ""};
            default            -> new String[]{"Quark.cc", "", "", ""};
        };
    }
}
