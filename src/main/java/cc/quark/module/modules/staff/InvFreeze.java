package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

public class InvFreeze extends Module {

    private final BoolSetting cancelClick = register(new BoolSetting(
            "Cancel Clicks", "Cancel slot-click packets to freeze inventory", true));
    private final BoolSetting cancelCreative = register(new BoolSetting(
            "Cancel Creative", "Also cancel creative-mode inventory actions", true));
    private final BoolSetting notifySelf = register(new BoolSetting(
            "Notify Self", "Show a local warning when an action is blocked", false));
    private final StringSetting targetName = register(new StringSetting(
            "Target", "Player name hint shown in logs (cosmetic)", ""));

    public InvFreeze() {
        super("InvFreeze", "Prevents players from modifying their inventory", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        mc.getEventBus().subscribe(this);
        String tgt = targetName.get().trim().isEmpty() ? "all" : targetName.get().trim();
        ChatUtil.info("§6[InvFreeze] §fInventory locked for: §e" + tgt);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        ChatUtil.info("§6[InvFreeze] §fInventory unlocked.");
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;

        if (cancelClick.isEnabled() && event.getPacket() instanceof ClickSlotC2SPacket) {
            event.cancel();
            if (notifySelf.isEnabled()) {
                ChatUtil.info("§6[InvFreeze] §7Blocked inventory click.");
            }
            return;
        }

        if (cancelCreative.isEnabled() && event.getPacket() instanceof CreativeInventoryActionC2SPacket) {
            event.cancel();
            if (notifySelf.isEnabled()) {
                ChatUtil.info("§6[InvFreeze] §7Blocked creative inventory action.");
            }
        }
    }
}
