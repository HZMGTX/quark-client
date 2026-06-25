package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.text.Text;

/**
 * CustomTabTitle - Overrides the tab-list header and footer with custom text.
 * Supports Minecraft color codes via § and dynamic placeholders.
 */
public class CustomTabTitle extends Module {

    private final BoolSetting overrideHeader = register(new BoolSetting(
            "Override Header", "Replace the tab header with custom text", true));

    private final StringSetting headerText = register(new StringSetting(
            "Header", "Tab header text (use & for color codes)", "&b&lQuark.cc &7| &fCustom Client"));

    private final BoolSetting overrideFooter = register(new BoolSetting(
            "Override Footer", "Replace the tab footer with custom text", true));

    private final StringSetting footerText = register(new StringSetting(
            "Footer", "Tab footer text", "&7FPS: {fps} &8| &7Ping: {ping}ms"));

    private final BoolSetting showFps = register(new BoolSetting(
            "Dynamic FPS", "Replace {fps} placeholder with current FPS", true));

    public CustomTabTitle() {
        super("CustomTabTitle", "Displays custom header and footer in the player tab list", Category.MISC);
    }

    @Override
    public void onDisable() {
        // Restore empty header/footer on disable
        if (mc.inGameHud != null) {
            mc.inGameHud.setDefaultTabListHeaderAndFooter();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.inGameHud == null) return;

        Text header = overrideHeader.isEnabled()
                ? Text.literal(buildText(headerText.get()))
                : Text.empty();

        Text footer = overrideFooter.isEnabled()
                ? Text.literal(buildText(footerText.get()))
                : Text.empty();

        mc.inGameHud.setTabListHeaderAndFooter(header, footer);
    }

    private String buildText(String raw) {
        // Convert & color codes to §
        String result = raw.replace("&", "§");

        // Replace dynamic placeholders
        if (showFps.isEnabled()) {
            int fps = mc.getCurrentFps();
            result = result.replace("{fps}", String.valueOf(fps));
        }

        if (mc.player != null && mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = entry != null ? entry.getLatency() : 0;
            result = result.replace("{ping}", String.valueOf(ping));
        }

        result = result.replace("{name}", mc.player != null
                ? mc.player.getName().getString() : "");

        return result;
    }
}
