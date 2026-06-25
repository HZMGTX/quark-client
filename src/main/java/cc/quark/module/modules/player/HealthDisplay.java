package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.text.Text;

public class HealthDisplay extends Module {

    private final BoolSetting actionBar = register(new BoolSetting(
            "ActionBar", "Show health in the action bar above the hotbar", true));

    private final BoolSetting chat = register(new BoolSetting(
            "Chat", "Send health to chat every update interval", false));

    private final TimerUtil timer = new TimerUtil();

    public HealthDisplay() {
        super("HealthDisplay", "Shows current health numerically in the action bar or chat", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(2000)) return;
        timer.reset();

        float health = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        String msg = String.format("Health: %.1f / %.1f", health, maxHealth);

        if (actionBar.isEnabled()) {
            mc.player.sendMessage(Text.literal(msg), true);
        }
        if (chat.isEnabled()) {
            ChatUtil.info(msg);
        }
    }
}
