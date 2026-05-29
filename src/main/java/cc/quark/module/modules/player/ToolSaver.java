package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

public class ToolSaver extends Module {

    private final IntSetting minDurability = register(new IntSetting(
            "Min Durability", "Block tool use when remaining durability is below this", 10, 5, 200));
    private final BoolSetting autoSwap = register(new BoolSetting(
            "Auto Swap", "Switch to backup tool if current is too low", true));
    private final BoolSetting warnInChat = register(new BoolSetting(
            "Warn In Chat", "Show warning in chat when tool is low", false));

    private final TimerUtil warnTimer = new TimerUtil();

    public ToolSaver() {
        super("ToolSaver", "Prevents using tools below minimum durability and swaps to backup", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || !held.isDamageable()) return;

        int remaining = held.getMaxDamage() - held.getDamage();
        if (remaining > minDurability.get()) return;

        // Warn player
        if (warnTimer.hasReached(3000)) {
            NotificationOverlay.send("ToolSaver",
                    held.getName().getString() + " has only " + remaining + " durability!",
                    NotificationOverlay.NotifType.WARNING);
            if (warnInChat.isEnabled()) {
                ChatUtil.warn("Low tool durability: " + remaining + " remaining on " + held.getName().getString());
            }
            warnTimer.reset();
        }

        // Auto-swap to a better pickaxe/axe
        if (autoSwap.isEnabled()) {
            int backup = InventoryUtil.findBestPickaxe();
            if (backup < 0) backup = InventoryUtil.findBestAxe();
            if (backup >= 0 && backup < 9 && backup != mc.player.getInventory().selectedSlot) {
                mc.player.getInventory().selectedSlot = backup;
            }
        }
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof PlayerInteractBlockC2SPacket)) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || !held.isDamageable()) return;

        int remaining = held.getMaxDamage() - held.getDamage();
        if (remaining <= minDurability.get()) {
            event.cancel();
        }
    }
}
