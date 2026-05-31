package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class AltManager extends Module {

    private final StringSetting defaultAlt = register(new StringSetting(
            "DefaultAlt", "Username of the default alt account to display", ""));

    private boolean notified = false;

    public AltManager() {
        super("AltManager", "Manages alt accounts for server switching", Category.MISC);
    }

    @Override
    public void onEnable() {
        notified = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (notified) return;
        notified = true;

        String alt = defaultAlt.get();
        if (!alt.isEmpty()) {
            ChatUtil.info("[AltManager] Default alt: §e" + alt);
        } else {
            ChatUtil.info("[AltManager] No default alt configured.");
        }
    }

    public String getDefaultAlt() {
        return defaultAlt.get();
    }
}
