package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.text.Text;

public class AutoLog extends Module {

    private final DoubleSetting health = register(new DoubleSetting(
            "Health", "Health threshold to disconnect at", 4.0, 1.0, 15.0));

    public AutoLog() {
        super("AutoLog", "Disconnects when health drops below threshold", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getHealth() <= (float) health.get()) {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal("AutoLog"));
        }
    }
}
