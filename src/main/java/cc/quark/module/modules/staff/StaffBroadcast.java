package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.text.Text;

public class StaffBroadcast extends Module {

    private final StringSetting message1 = register(new StringSetting("Message1", "Message1", "Welcome to the server!"));
    private final StringSetting message2 = register(new StringSetting("Message2", "Message2", "Read the rules at /rules"));
    private final StringSetting message3 = register(new StringSetting("Message3", "Message3", "Need help? Ask a staff member."));
    private final IntSetting interval    = register(new IntSetting("Interval", "Interval", 300, 60, 1200));
    private final BoolSetting cycle      = register(new BoolSetting("Cycle", "Cycle", true));

    private int timer = 0;
    private int msgIndex = 0;

    public StaffBroadcast() {
        super("StaffBroadcast", "Automatically broadcasts messages to the server at set intervals", Category.STAFF);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
        if (mc == null || mc.player == null) return;
        if (++timer < interval.get() * 20) return;
        timer = 0;

        String[] messages = { message1.get(), message2.get(), message3.get() };
        String msg = messages[msgIndex % messages.length];
        if (!msg.isBlank()) {
            mc.player.networkHandler.sendChatMessage(msg);
        }
        if (cycle.isEnabled()) msgIndex++;
    }
}
