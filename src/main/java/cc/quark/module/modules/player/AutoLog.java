package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;

public class AutoLog extends Module {

    private final DoubleSetting hpThreshold = register(new DoubleSetting(
            "HP Threshold", "Health (half-hearts) below which auto-disconnect triggers", 3.0, 1.0, 20.0));

    private final BoolSetting confirm = register(new BoolSetting(
            "Confirm", "Send a chat message before disconnecting", false));

    private boolean triggered = false;

    public AutoLog() {
        super("AutoLog", "Auto-disconnects when HP drops below threshold", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        triggered = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (triggered) return;

        float health = mc.player.getHealth();
        if (health > (float) hpThreshold.get()) return;

        triggered = true;

        if (confirm.isEnabled()) {
            cc.quark.util.ChatUtil.info("AutoLog: disconnecting (HP: " + String.format("%.1f", health) + ")");
        }

        mc.execute(() -> mc.world.disconnect());
        mc.disconnect();
    }
}
