package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.text.Text;

/**
 * AutoDisconnect — disconnects from the server when health falls below
 * a configurable threshold.
 *
 * Modes:
 *   Damage — triggered when a single hit deals >= Threshold damage.
 *   Health — triggered each tick when HP <= Threshold.
 */
public class AutoDisconnect extends Module {

    private final ModeSetting  mode      = register(new ModeSetting ("Mode",      "When to disconnect",              "Health", "Health", "Damage"));
    private final DoubleSetting threshold = register(new DoubleSetting("Threshold","HP threshold (Health) / damage value (Damage)", 6.0, 1.0, 20.0));

    public AutoDisconnect() {
        super("AutoDisconnect", "Disconnects from the server when health is critical", Category.COMBAT);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (!mode.is("Damage")) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (event.getAmount() >= threshold.get()) {
            disconnect("damage " + String.format("%.1f", event.getAmount()));
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!mode.is("Health")) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getHealth() <= (float) threshold.get()) {
            disconnect("HP " + String.format("%.1f", mc.player.getHealth()));
        }
    }

    private void disconnect(String reason) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getConnection().disconnect(
                    Text.literal("[AutoDisconnect] " + reason));
        }
    }
}
