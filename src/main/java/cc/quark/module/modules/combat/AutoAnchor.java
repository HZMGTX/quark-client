package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * AutoAnchor — auto-places and activates a Respawn Anchor at the nearest
 * enemy player's feet.  Only functions in the Nether (anchors only charge
 * there; they explode in the Overworld/End which is handled by AnchorAura).
 */
public class AutoAnchor extends Module {

    private final IntSetting  range      = register(new IntSetting ("Range",       "Range to search for targets (blocks)", 6, 2, 16));
    private final IntSetting  delayMs    = register(new IntSetting ("Delay",        "Milliseconds between activations",    500, 100, 2000));
    private final BoolSetting netherOnly = register(new BoolSetting("Nether Only",  "Only activate in the Nether",          true));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoAnchor() {
        super("AutoAnchor", "Auto-places and activates Respawn Anchors at enemy feet (Nether)", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (netherOnly.isEnabled() && mc.world.getRegistryKey() != World.NETHER) return;
        if (!timer.hasReached(delayMs.get())) return;

        // Find nearest enemy player
        PlayerEntity target = null;
        double bestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isRemoved() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist) { bestDist = d; target = p; }
        }
        if (target == null) { restoreSlot(); return; }

        // Find anchor in hotbar
        int anchorSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.RESPAWN_ANCHOR)) { anchorSlot = i; break; }
        }
        if (anchorSlot == -1) { restoreSlot(); return; }

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != anchorSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = anchorSlot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
