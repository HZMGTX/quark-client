package com.ghostclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final String PREFIX = Formatting.DARK_GRAY + "[" + Formatting.AQUA + "Ghost" + Formatting.DARK_GRAY + "] " + Formatting.RESET;

    public static void send(String message) {
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatMessage(message);
    }

    public static void addMessage(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(Text.literal(PREFIX + message), false);
    }

    public static void info(String message) {
        addMessage(Formatting.GRAY + message);
    }

    public static void error(String message) {
        addMessage(Formatting.RED + message);
    }

    public static void success(String message) {
        addMessage(Formatting.GREEN + message);
    }

    public static void warn(String message) {
        addMessage(Formatting.YELLOW + message);
    }
}
