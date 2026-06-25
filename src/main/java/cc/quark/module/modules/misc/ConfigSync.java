package cc.quark.module.modules.misc;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

import java.awt.*;
import java.awt.datatransfer.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConfigSync extends Module {

    private final BoolSetting autoLoad = register(new BoolSetting(
            "AutoLoad", "Automatically load config from clipboard on enable", false));

    public ConfigSync() {
        super("ConfigSync", "Syncs module configs between client sessions via base64 clipboard", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (autoLoad.isEnabled()) {
            load();
        }
    }

    public void export(String json) {
        try {
            String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            StringSelection sel = new StringSelection(encoded);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            ChatUtil.info("[ConfigSync] Config exported to clipboard.");
        } catch (Exception e) {
            ChatUtil.warn("[ConfigSync] Export failed: " + e.getMessage());
        }
    }

    public void load() {
        try {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            String encoded = (String) cb.getData(DataFlavor.stringFlavor);
            byte[] decoded = Base64.getDecoder().decode(encoded.trim());
            String json = new String(decoded, StandardCharsets.UTF_8);
            ChatUtil.info("[ConfigSync] Loaded config (" + json.length() + " chars).");
        } catch (Exception e) {
            ChatUtil.warn("[ConfigSync] Load failed: " + e.getMessage());
        }
    }
}
