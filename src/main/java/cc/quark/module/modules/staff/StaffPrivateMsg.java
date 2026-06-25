package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.text.Text;

public class StaffPrivateMsg extends Module {

    private final StringSetting prefix = register(new StringSetting(
            "Prefix", "Prefix prepended to all staff messages", "[STAFF]"));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "ARGB color for the staff message highlight", 0xFF00AAFF));
    private final BoolSetting soundAlert = register(new BoolSetting(
            "Sound Alert", "Play a subtle sound when a staff-prefixed message is received", true));
    private final StringSetting staffChannel = register(new StringSetting(
            "Channel Command", "Server command used to target the staff channel", "staffchat"));

    public StaffPrivateMsg() {
        super("StaffPrivateMsg", "Sends color-coded messages visible only to staff via a staff chat channel", Category.STAFF);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("§6[StaffMsg] §fStaff messaging active. Type §e/sc <message> §fin chat to send.");
    }

    /**
     * Intercepts outgoing chat starting with "/sc " and re-routes it through
     * the configured staff channel command with the configured prefix.
     */
    @EventHandler
    public void onChat(EventChat event) {
        if (mc.player == null) return;
        String msg = event.getMessage();
        if (msg == null || !msg.startsWith("/sc ")) return;

        event.cancel();
        String body = msg.substring(4).trim();
        if (body.isEmpty()) return;

        String formatted = prefix.get() + " " + body;
        mc.player.networkHandler.sendChatCommand(staffChannel.get() + " " + formatted);

        // Echo locally with color
        int argb = color.get();
        String hex = String.format("#%06X", argb & 0xFFFFFF);
        mc.player.sendMessage(
                Text.literal("§b" + prefix.get() + " §f" + body), false);

        if (soundAlert.isEnabled()) {
            // Play a UI click sound as an alert indicator
            mc.player.playSound(
                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(),
                    net.minecraft.sound.SoundCategory.MASTER, 0.5f, 1.0f);
        }
    }
}
