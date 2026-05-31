package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

/**
 * AntiWither2 - automatically consumes a Milk Bucket from the hotbar to cancel
 * the Wither status effect (and any other negative effects).
 *
 * <p>When the player has the Wither effect and a Milk Bucket is available,
 * this module switches to that slot, right-clicks to drink, then restores the
 * original slot.
 */
public class AntiWither2 extends Module {

    private final BoolSetting antiPoison = register(new BoolSetting(
            "Anti Poison", "Also use milk for Poison effect", false));

    private final BoolSetting antiWeakness = register(new BoolSetting(
            "Anti Weakness", "Also use milk for Weakness effect", false));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown", "Minimum ms between milk uses", 2000, 500, 10000));

    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Return to original slot after drinking", true));

    private final TimerUtil milkTimer = new TimerUtil();

    public AntiWither2() {
        super("AntiWither2", "Auto-drinks milk to cancel Wither effect", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        milkTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!milkTimer.hasReached(cooldownMs.get())) return;

        boolean needsMilk = mc.player.hasStatusEffect(StatusEffects.WITHER)
                || (antiPoison.isEnabled()   && mc.player.hasStatusEffect(StatusEffects.POISON))
                || (antiWeakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS));

        if (!needsMilk) return;

        int milkSlot = findMilkSlot();
        if (milkSlot == -1) return;

        int previousSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = milkSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(milkSlot));

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        if (restoreSlot.isEnabled()) {
            mc.player.getInventory().selectedSlot = previousSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }

        ChatUtil.info("[AntiWither2] Drank milk to cure effects.");
        milkTimer.reset();
    }

    private int findMilkSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.MILK_BUCKET) {
                return i;
            }
        }
        return -1;
    }
}
