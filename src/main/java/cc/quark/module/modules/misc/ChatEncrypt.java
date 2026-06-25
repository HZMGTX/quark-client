package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ChatEncrypt extends Module {

    private final StringSetting key       = register(new StringSetting("Key", "Shared encryption key (must match all parties)", "secret123"));
    private final StringSetting prefix    = register(new StringSetting("Prefix", "Tag prepended to encrypted messages", "ENC:"));
    private final BoolSetting   autoDecrypt = register(new BoolSetting("Auto Decrypt", "Automatically decrypt matching incoming messages", true));
    private final BoolSetting   hideRaw     = register(new BoolSetting("Hide Raw", "Replace raw cipher text in chat with decrypted form", true));

    public ChatEncrypt() {
        super("ChatEncrypt", "Encrypts outgoing chat messages and decrypts incoming ones with a shared XOR key", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        String msg = event.getMessage();
        String pfx = prefix.get();

        if (event.isIncoming()) {
            // Decrypt incoming
            if (!autoDecrypt.isEnabled()) return;
            if (!msg.contains(pfx)) return;

            int start = msg.indexOf(pfx);
            String cipher = msg.substring(start + pfx.length()).trim();
            String plain = decrypt(cipher);

            if (hideRaw.isEnabled() && plain != null) {
                String newMsg = msg.substring(0, start) + plain;
                event.setMessage(newMsg);
            }
        } else {
            // Encrypt outgoing
            if (msg.startsWith("/")) return; // don't encrypt commands
            String cipher = encrypt(msg);
            if (cipher != null) {
                event.setMessage(pfx + cipher);
            }
        }
    }

    /** XOR cipher with key, Base64 encoded. */
    private String encrypt(String plain) {
        try {
            byte[] data = plain.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = key.get().getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                out[i] = (byte)(data[i] ^ keyBytes[i % keyBytes.length]);
            }
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            return null;
        }
    }

    /** Reverse of encrypt. */
    private String decrypt(String cipher) {
        try {
            byte[] data = Base64.getDecoder().decode(cipher);
            byte[] keyBytes = key.get().getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                out[i] = (byte)(data[i] ^ keyBytes[i % keyBytes.length]);
            }
            return new String(out, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
