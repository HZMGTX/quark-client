package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

public class ClearChat extends Module {

    private final IntSetting lines = register(new IntSetting(
            "Lines", "Number of blank lines to spam into chat", 100, 10, 200));
    private final IntSetting delayTicks = register(new IntSetting(
            "Delay Ticks", "Ticks between each blank line send", 1, 1, 10));
    private final BoolSetting notifyAfter = register(new BoolSetting(
            "Notify", "Send a 'Chat cleared' message after spamming", true));

    private int linesRemaining = 0;
    private int tickCounter = 0;

    public ClearChat() {
        super("ClearChat", "Spams empty lines to clear everyone's chat", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        linesRemaining = lines.get();
        tickCounter = 0;
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[ClearChat] §fSpamming §e" + linesRemaining + " §fblank lines...");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        linesRemaining = 0;
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) { disable(); return; }
        if (linesRemaining <= 0) {
            if (notifyAfter.isEnabled()) {
                mc.player.networkHandler.sendChatCommand("say  *** Chat cleared by staff ***");
            }
            disable();
            return;
        }

        if (++tickCounter < delayTicks.get()) return;
        tickCounter = 0;

        // Send a blank /say line
        mc.player.networkHandler.sendChatCommand("say  ");
        linesRemaining--;
    }
}
