package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;

/**
 * QuickShield — automatically raises the player's shield upon taking damage
 * and holds it raised for a configurable number of ticks.
 */
public class QuickShield extends Module {

    private final IntSetting holdTicks = register(new IntSetting(
            "HoldTicks", "Number of ticks to hold the shield raised after damage", 10, 1, 40));

    private int shieldTimer = 0;

    public QuickShield() {
        super("QuickShield", "Automatically raises shield on taking damage", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        shieldTimer = 0;
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        if (!hasShield()) return;

        // Start holding the shield
        mc.options.useKey.setPressed(true);
        shieldTimer = holdTicks.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (shieldTimer <= 0) return;

        shieldTimer--;
        if (shieldTimer <= 0) {
            mc.options.useKey.setPressed(false);
        }
    }

    private boolean hasShield() {
        if (mc.player == null) return false;
        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();
        return main.isOf(Items.SHIELD) || off.isOf(Items.SHIELD);
    }
}
