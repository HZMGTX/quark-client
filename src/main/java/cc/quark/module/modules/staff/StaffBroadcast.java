package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.IntSetting;
import cc.quark.module.setting.StringSetting;
import net.minecraft.text.Text;

public class StaffBroadcast extends Module {

    private final StringSetting message1 = new StringSetting("Message1", "Welcome to the server!");
    private final StringSetting message2 = new StringSetting("Message2", "Read the rules at /rules");
    private final StringSetting message3 = new StringSetting("Message3", "Need help? Ask a staff member.");
    private final IntSetting interval    = new IntSetting("Interval", 300, 60, 1200);
    private final BoolSetting cycle      = new BoolSetting("Cycle", true);

    private int timer = 0;
    private int msgIndex = 0;

    public StaffBroadcast() {
        super("StaffBroadcast", "Automatically broadcasts messages to the server at set intervals", Category.STAFF);
        addSettings(message1, message2, message3, interval, cycle);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); timer = 0; msgIndex = 0; }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
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
