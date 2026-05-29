package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class AutoRepair extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Remaining durability percent to trigger repair", 10, 1, 50));
    private final BoolSetting useAnvil = register(new BoolSetting(
            "Use Anvil", "Auto-interact with nearby anvil", true));
    private final BoolSetting warnOnly = register(new BoolSetting(
            "Warn Only", "Only warn in chat, do not attempt auto-repair", false));

    private final TimerUtil warnTimer = new TimerUtil();
    private boolean warned = false;

    public AutoRepair() {
        super("AutoRepair", "Warns or auto-uses a nearby anvil when item durability is low", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        warned = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || !held.isDamageable()) {
            warned = false;
            return;
        }

        int remaining = held.getMaxDamage() - held.getDamage();
        int pct = (int) (100.0 * remaining / (double) held.getMaxDamage());

        if (pct > threshold.get()) {
            warned = false;
            return;
        }

        // Warn player
        if (!warned && warnTimer.hasReached(4000)) {
            ChatUtil.warn("Low durability on held item (" + pct + "% remaining)!");
            NotificationOverlay.send("AutoRepair", held.getName().getString() + " is at " + pct + "%", NotificationOverlay.NotifType.WARNING);
            warned = true;
            warnTimer.reset();
        }

        // Attempt to use a nearby anvil
        if (!warnOnly.isEnabled() && useAnvil.isEnabled()
                && mc.interactionManager != null
                && mc.player.currentScreenHandler instanceof AnvilScreenHandler) {
            // Put item in first slot and attempt to rename/repair it
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    0, 0, SlotActionType.PICKUP, mc.player);
        }
    }
}
