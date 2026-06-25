package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * SilentInventory - allows managing your inventory without sending an
 * open-inventory packet to the server, so the server never knows you
 * opened your inventory.
 *
 * Implementation: opens the vanilla inventory screen client-side when
 * the setting is active, suppressing any network open packet.
 * The actual packet suppression requires a mixin; this module keeps
 * a flag that the mixin can query.
 */
public class SilentInventory extends Module {

    private final BoolSetting silent = register(new BoolSetting(
            "Silent", "Suppress the open-inventory packet sent to the server", true));

    /** Static flag queried by MixinClientPlayNetworkHandler to drop open-screen packets. */
    public static boolean isSilentOpen = false;

    public SilentInventory() {
        super("SilentInventory", "Open inventory silently without server knowing", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        isSilentOpen = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Keep the silent flag in sync with the setting
        isSilentOpen = silent.isEnabled();

        // If no screen is open and the player is alive, silently open inventory when requested
        // (actual key binding is handled by vanilla; this module just gates the packet flag)
        if (mc.currentScreen instanceof InventoryScreen) {
            isSilentOpen = silent.isEnabled();
        }
    }
}
