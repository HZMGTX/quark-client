package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoShield extends Module {

    private final IntSetting reactionMs = register(new IntSetting(
            "ReactionMs", "Delay in milliseconds before blocking with shield", 50, 0, 500));

    private final TimerUtil damageTimer = new TimerUtil();
    private boolean wasDamaged = false;
    private float lastHealth = 20f;

    public AutoShield() {
        super("AutoShield", "Auto-blocks with shield when taking damage", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float currentHealth = mc.player.getHealth();

        // Detect damage
        if (currentHealth < lastHealth) {
            wasDamaged = true;
            damageTimer.reset();
        }
        lastHealth = currentHealth;

        // Ensure shield is in offhand
        ensureShieldInOffhand();

        if (!hasShieldInOffhand()) {
            mc.options.useKey.setPressed(false);
            return;
        }

        if (wasDamaged && damageTimer.hasReached(reactionMs.get())) {
            mc.options.useKey.setPressed(true);
            // Keep blocking for a short time after damage
            if (damageTimer.hasReached(reactionMs.get() + 600L)) {
                mc.options.useKey.setPressed(false);
                wasDamaged = false;
            }
        } else if (!wasDamaged) {
            mc.options.useKey.setPressed(false);
        }
    }

    private boolean hasShieldInOffhand() {
        if (mc.player == null) return false;
        return mc.player.getOffHandStack().getItem() == Items.SHIELD;
    }

    private void ensureShieldInOffhand() {
        if (hasShieldInOffhand()) return;

        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == Items.SHIELD) {
                int screenSlot = i < 9 ? 36 + i : i;
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        screenSlot, 40, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }
}
