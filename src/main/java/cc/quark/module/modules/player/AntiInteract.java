package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

/**
 * AntiInteract - Prevents accidental interactions with blocks, items, and
 * entities by cancelling the corresponding outgoing packets. Useful during
 * combat or when navigating crowded areas.
 */
public class AntiInteract extends Module {

    private final BoolSetting cancelBlock = register(new BoolSetting(
            "Cancel Blocks", "Block right-click interactions with blocks", true));

    private final BoolSetting cancelItem = register(new BoolSetting(
            "Cancel Items", "Block right-click item use (food, potions, etc.)", false));

    private final BoolSetting cancelEntity = register(new BoolSetting(
            "Cancel Entities", "Block interactions with entities (trading, riding, etc.)", false));

    private final BoolSetting onlyWhileSprinting = register(new BoolSetting(
            "Only Sprinting", "Only block interactions while sprinting", true));

    public AntiInteract() {
        super("AntiInteract", "Prevents accidental GUI interactions with blocks/entities", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (onlyWhileSprinting.isEnabled() && !mc.player.isSprinting()) return;

        if (cancelBlock.isEnabled() && event.getPacket() instanceof PlayerInteractBlockC2SPacket) {
            event.cancel();
        } else if (cancelItem.isEnabled() && event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            event.cancel();
        } else if (cancelEntity.isEnabled() && event.getPacket() instanceof PlayerInteractEntityPacket) {
            event.cancel();
        }
    }
}
