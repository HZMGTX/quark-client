package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.StringSetting;
import net.minecraft.text.Text;

public class StaffVanish extends Module {

    private final BoolSetting fakeLeaveMsg  = new BoolSetting("FakeLeaveMsg", true);
    private final BoolSetting silentMove    = new BoolSetting("SilentMove", true);
    private final StringSetting vanishCmd   = new StringSetting("VanishCommand", "/vanish");

    public StaffVanish() {
        super("StaffVanish", "Enhanced vanish: fake leave message, silent movement, staff-only visibility", Category.STAFF);
        addSettings(fakeLeaveMsg, silentMove, vanishCmd);
    }

    @Override
    public void onEnable() {
        Quark.mc.getEventBus().subscribe(this);
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendChatCommand(vanishCmd.get().replace("/", ""));
        if (fakeLeaveMsg.isEnabled()) {
            mc.player.sendMessage(Text.literal("§7[StaffVanish] §fVanish enabled — players see you as offline"), false);
        }
    }

    @Override
    public void onDisable() {
        Quark.mc.getEventBus().unsubscribe(this);
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendChatCommand(vanishCmd.get().replace("/", ""));
        mc.player.sendMessage(Text.literal("§7[StaffVanish] §fVanish disabled"), false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        if (silentMove.isEnabled()) {
            // Suppress movement sounds while vanished
            mc.player.setSneaking(true);
        }
    }
}
