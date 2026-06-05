package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class ChatColors extends Module {

    private final ModeSetting color = register(new ModeSetting(
            "Color", "Color to prepend to outgoing messages",
            "Aqua", "Aqua", "Green", "Yellow", "Red", "Light Purple", "Gold", "White", "Gray"));

    private final BoolSetting bold   = register(new BoolSetting("Bold",   "Apply bold formatting",   false));
    private final BoolSetting italic = register(new BoolSetting("Italic", "Apply italic formatting", false));
    private final BoolSetting obfuscate = register(new BoolSetting("Obfuscate", "Obfuscate the text (matrix effect)", false));

    public ChatColors() {
        super("ChatColors", "Applies Minecraft color/style formatting to outgoing chat messages", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;
        // Don't colorize commands
        if (msg.startsWith("/")) return;

        StringBuilder prefix = new StringBuilder();
        prefix.append(colorCode());
        if (bold.isEnabled())      prefix.append("§l");
        if (italic.isEnabled())    prefix.append("§o");
        if (obfuscate.isEnabled()) prefix.append("§k");

        event.setMessage(prefix + msg);
    }

    private String colorCode() {
        return switch (color.get()) {
            case "Green"        -> "§a";
            case "Yellow"       -> "§e";
            case "Red"          -> "§c";
            case "Light Purple" -> "§d";
            case "Gold"         -> "§6";
            case "White"        -> "§f";
            case "Gray"         -> "§7";
            default             -> "§b"; // Aqua
        };
    }

    @Override
    public String getSuffix() {
        return color.get();
    }
}
