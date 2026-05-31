package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

/**
 * AntiSteal — detects and prevents other players (or the server) from
 * modifying the player's inventory slots without the player initiating the
 * action. When the server attempts to push an unexpected slot update into
 * a protected slot, the packet is cancelled and the player is notified.
 */
public class AntiSteal extends Module {

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Warn in chat when a steal attempt is detected", true));

    private final BoolSetting protectHotbar = register(new BoolSetting(
            "Protect Hotbar", "Protect hotbar slots 0-8", true));

    private final BoolSetting protectArmor = register(new BoolSetting(
            "Protect Armor", "Protect armor slots", true));

    private final BoolSetting protectOffhand = register(new BoolSetting(
            "Protect Offhand", "Protect the offhand slot", true));

    // Track whether the player has an open screen (chest, etc.)
    // If a screen is open, slot updates from the server are legitimate
    private boolean screenOpen = false;

    public AntiSteal() {
        super("AntiSteal", "Prevents other players from modifying your inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        screenOpen = mc.currentScreen != null;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        // If the player has a GUI open, slot changes may be legitimate (e.g., chest trading)
        if (screenOpen) return;

        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket pkt) {
            // syncId 0 = player inventory; any other id = open container
            if (pkt.getSyncId() != 0) return;

            int slot = pkt.getSlot();
            if (isProtectedSlot(slot)) {
                event.cancel();
                if (notify.isEnabled()) {
                    ChatUtil.warn("AntiSteal: blocked server inventory modification (slot " + slot + ")");
                }
            }
        }

        // Full inventory replacement — block if we didn't open any GUI
        if (event.getPacket() instanceof InventoryS2CPacket pkt) {
            if (pkt.getSyncId() != 0) return;
            event.cancel();
            if (notify.isEnabled()) {
                ChatUtil.warn("AntiSteal: blocked full inventory sync packet");
            }
        }
    }

    /**
     * Returns true for slots that correspond to hotbar, armor, or offhand
     * in the player's inventory screen (syncId 0).
     *
     * Slot layout (player inventory, syncId=0):
     *   0         = crafting output
     *   1-4       = crafting grid
     *   5-8       = armor (head=5, chest=6, legs=7, feet=8)
     *   9-35      = main inventory (rows 0-2)
     *   36-44     = hotbar
     *   45        = offhand
     */
    private boolean isProtectedSlot(int slot) {
        if (protectHotbar.isEnabled() && slot >= 36 && slot <= 44) return true;
        if (protectArmor.isEnabled() && slot >= 5 && slot <= 8) return true;
        if (protectOffhand.isEnabled() && slot == 45) return true;
        return false;
    }
}
