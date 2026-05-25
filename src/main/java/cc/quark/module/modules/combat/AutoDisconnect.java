package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.text.Text;

public class AutoDisconnect extends Module {

    private final DoubleSetting minHealth = register(new DoubleSetting(
            "Min Health", "Disconnect when HP drops below this value (half-hearts)", 6.0, 1.0, 18.0));

    public AutoDisconnect() {
        super("AutoDisconnect", "Disconnects from the server when health falls below a threshold", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getHealth() <= (float) minHealth.get()) {
            mc.getNetworkHandler().getConnection().disconnect(
                    Text.literal("AutoDisconnect: HP critical (" + (int) mc.player.getHealth() + ")"));
        }
    }
}
