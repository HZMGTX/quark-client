package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.Text;

public class TitleManager extends Module {

    private final StringSetting title    = register(new StringSetting("Title",    "Title text shown on join", "Welcome!"));
    private final StringSetting subtitle = register(new StringSetting("Subtitle", "Subtitle text shown on join", ""));

    public TitleManager() {
        super("TitleManager", "Shows custom title and subtitle text when joining a server", Category.MISC);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof GameJoinS2CPacket)) return;
        mc.execute(() -> {
            if (mc.inGameHud == null) return;
            mc.inGameHud.setTitle(Text.literal(title.get()));
            mc.inGameHud.setSubtitle(Text.literal(subtitle.get()));
            mc.inGameHud.setTitleTicks(10, 60, 20);
        });
    }
}
