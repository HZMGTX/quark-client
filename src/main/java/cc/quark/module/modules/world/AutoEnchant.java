package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoEnchant extends Module {
    private final ModeSetting level = register(new ModeSetting("Level", "Enchant level to use", "Max", "Max", "Mid", "Min"));
    private final TimerUtil timer = new TimerUtil();

    public AutoEnchant() { super("AutoEnchant", "Auto-selects best enchantment in enchanting table", Category.WORLD); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof EnchantmentScreenHandler handler)) return;
        if (!timer.hasReached(500)) return;
        int slot = switch (level.get()) {
            case "Max" -> 2;
            case "Mid" -> 1;
            case "Min" -> 0;
            default -> 2;
        };
        if (handler.getEnchantmentId(slot) >= 0) {
            mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            ChatUtil.info("Enchanting with slot " + slot);
            timer.reset();
        }
    }
}
