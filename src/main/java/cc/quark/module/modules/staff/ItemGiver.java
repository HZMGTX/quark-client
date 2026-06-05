package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class ItemGiver extends Module {
    private final StringSetting target = register(new StringSetting("Target", "Player name (@ for self)", "@s"));
    private final StringSetting item = register(new StringSetting("Item", "Item ID", "minecraft:diamond"));
    private final IntSetting amount = register(new IntSetting("Amount", "Item count", 64, 1, 64));
    private boolean executed = false;

    public ItemGiver() { super("ItemGiver", "Gives items to players using /give", Category.STAFF); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); executed = false; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || executed) return;
        String cmd = "give " + target.get() + " " + item.get() + " " + amount.get();
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("Gave " + amount.get() + "x " + item.get() + " to " + target.get());
        executed = true;
        disable();
    }
}
