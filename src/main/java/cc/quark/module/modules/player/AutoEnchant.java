package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.screen.EnchantmentScreenHandler;

public class AutoEnchant extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max distance from enchanting table to trigger auto-enchant", 3.0, 1.0, 6.0));

    private final IntSetting level = register(new IntSetting(
            "Level", "Enchantment level slot to select (1-3)", 30, 1, 30));

    private final TimerUtil timer = new TimerUtil();

    public AutoEnchant() {
        super("AutoEnchant", "Auto-enchants items at enchanting table", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof EnchantmentScreen)) return;
        if (!timer.hasReached(600)) return;

        EnchantmentScreenHandler handler = (EnchantmentScreenHandler) mc.player.currentScreenHandler;

        // Determine which slot index to click (0=low, 1=mid, 2=high)
        int slot = Math.min(2, Math.max(0, (level.get() / 11)));

        // Check that enchantment is available (power > 0)
        int[] powers = handler.enchantmentPower;
        if (powers == null || powers.length <= slot || powers[slot] <= 0) return;

        mc.interactionManager.clickButton(handler.syncId, slot);
        timer.reset();
    }
}
