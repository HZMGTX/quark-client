package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

public class KillAlert extends Module {

    private final BoolSetting sound = register(new BoolSetting("Sound", "Play a ding sound on kill detection", true));

    public KillAlert() {
        super("KillAlert", "Notifies when you kill a player based on kill messages in chat", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();
        String selfName = mc.player.getName().getString();

        boolean isKillMessage = msg.contains(selfName) && (
                msg.contains("was slain by") ||
                msg.contains("was shot by") ||
                msg.contains("was killed by") ||
                msg.contains("was blown up by") ||
                msg.contains("was fireballed by") ||
                msg.contains("was pummeled by")
        );

        if (isKillMessage) {
            ChatUtil.addMessage("§a[KillAlert] §fYou got a kill!");
            if (sound.isEnabled() && mc.world != null) {
                mc.execute(() -> mc.player.playSound(
                        net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        1.0f, 1.0f));
            }
        }
    }
}
