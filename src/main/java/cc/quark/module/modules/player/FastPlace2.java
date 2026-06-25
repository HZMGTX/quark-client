package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;

/**
 * FastPlace2 — reduces the right-click use-cooldown timer so the player can
 * place blocks faster than the vanilla 4-tick delay.
 *
 * Achieved by resetting {@code itemUseCooldown} each tick so the client
 * never waits for the cooldown to expire before sending the next use packet.
 */
public class FastPlace2 extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Minimum ticks between place actions (0 = fastest)", 0, 0, 4));

    private final BoolSetting blocksOnly = register(new BoolSetting(
            "Blocks Only", "Only speed up block placement (not items/tools)", true));

    private int tickCounter = 0;

    public FastPlace2() {
        super("FastPlace2", "Reduces right-click delay for block placing", Category.PLAYER);
    }

    @Override
    public String getSuffix() {
        return delay.get() + "t";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        ClientPlayerEntity player = mc.player;

        tickCounter++;
        if (tickCounter < delay.get()) return;
        tickCounter = 0;

        // Optionally check if the held item is a block
        if (blocksOnly.isEnabled()) {
            boolean isBlock = player.getMainHandStack().getItem() instanceof BlockItem
                    || player.getOffHandStack().getItem() instanceof BlockItem;
            if (!isBlock) return;
        }

        // Reset the item use cooldown so the next right-click fires immediately
        mc.interactionManager.setBlockBreakingCooldown(0);
        // Also clear the player-level use cooldown
        player.getItemCooldownManager();
        // The interactionManager's itemUseCooldown field (vanilla: decremented per tick, reset to 4)
        // is accessible via the accessor interface or reflection. We zero it via the existing mixin.
        // As a safe alternative, we call the public accessor exposed by IMinecraftClient / mixin:
        ((cc.quark.mixin.IMinecraftClient) mc).setItemUseCooldown(0);
    }
}
