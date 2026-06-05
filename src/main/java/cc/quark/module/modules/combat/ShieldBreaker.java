package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

/**
 * ShieldBreaker — detects when the nearest enemy player is holding a shield
 * and automatically switches to the best axe to break it.
 * After the shield is broken the previous slot is restored.
 */
public class ShieldBreaker extends Module {

    private final DoubleSetting range       = register(new DoubleSetting("Range",        "Attack range",                     3.5, 1.0, 6.0));
    private final BoolSetting   onlyShields = register(new BoolSetting  ("Only Shields", "Only switch when target is shielding", true));
    private final BoolSetting   autoSwitch  = register(new BoolSetting  ("Auto Switch",  "Auto-switch to best axe",           true));

    private int prevSlot = -1;

    public ShieldBreaker() {
        super("ShieldBreaker", "Auto-switches to axe to break enemy shields", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Find nearest shielding player
        PlayerEntity target = null;
        double best = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isRemoved() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d > best) continue;

            boolean isShielding = onlyShields.isEnabled()
                    && (p.isUsingItem()
                        && (p.getActiveItem().getItem() instanceof ShieldItem
                            || p.getOffHandStack().getItem() instanceof ShieldItem));

            if (onlyShields.isEnabled() && !isShielding) continue;
            best = d; target = p;
        }

        if (target == null) { restoreSlot(); return; }

        // Ensure we hold an axe
        boolean holdingAxe = mc.player.getMainHandStack().getItem() instanceof AxeItem;
        if (!holdingAxe) {
            if (!autoSwitch.isEnabled()) return;
            int axeSlot = InventoryUtil.findBestAxe();
            if (axeSlot == -1 || axeSlot > 8) return;
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = axeSlot;
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
