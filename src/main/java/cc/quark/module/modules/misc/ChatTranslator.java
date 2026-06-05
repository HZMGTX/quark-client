package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ChatTranslator extends Module {

    private final ModeSetting targetLang = register(new ModeSetting(
            "Language", "Target translation language",
            "English", "English", "Spanish", "French", "German", "Portuguese", "Russian", "Japanese", "Chinese"));
    private final BoolSetting onlyForeign = register(new BoolSetting(
            "Only Foreign", "Only translate non-English messages", true));

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final java.util.Map<String, String> LANG_CODES = java.util.Map.of(
        "English", "en", "Spanish", "es", "French", "fr", "German", "de",
        "Portuguese", "pt", "Russian", "ru", "Japanese", "ja", "Chinese", "zh"
    );

    public ChatTranslator() {
        super("ChatTranslator", "Auto-translates incoming chat messages", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String message = event.getMessage();
        if (message == null || message.length() < 3) return;

        String langCode = LANG_CODES.getOrDefault(targetLang.get(), "en");

        // Async translation to avoid blocking game thread
        new Thread(() -> {
            try {
                String translated = translateLibre(message, langCode);
                if (translated != null && !translated.equalsIgnoreCase(message)) {
                    mc.execute(() -> {
                        if (mc.player != null) {
                            mc.player.sendMessage(Text.literal("§7[Translated] §f" + translated), false);
                        }
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private String translateLibre(String text, String targetLang) {
        try {
            String body = "q=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                        + "&source=auto"
                        + "&target=" + targetLang
                        + "&format=text";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://libretranslate.de/translate"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body();

            // Simple JSON extraction: "translatedText":"..."
            int idx = json.indexOf("\"translatedText\":");
            if (idx < 0) return null;
            int start = json.indexOf('"', idx + 17) + 1;
            int end   = json.indexOf('"', start);
            if (start <= 0 || end <= start) return null;
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}
