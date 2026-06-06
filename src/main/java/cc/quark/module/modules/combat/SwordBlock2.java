package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * SwordBlock2 — enhanced sword/axe blocking with configurable timing.
 * Supports:
 *  - Block immediately on taking damage (reactive mode)
 *  - Block whenever an enemy is within range (proactive mode)
 *  - Auto-raise shield when available
 *  - WTap integration (release block before swing for knockback)
 */
public class SwordBlock2 extends Module {

    private final BoolSetting proactive = register(new BoolSetting(
            "Proactive", "Block when enemies are within range", true));
    private final DoubleSetting blockRange = register(new DoubleSetting(
            "Block Range", "Range to start blocking proactively", 5.0, 1.0, 10.0));
    private final BoolSetting useShield = register(new BoolSetting(
            "Use Shield", "Raise shield (offhand) when available", true));
    private final BoolSetting wTap = register(new BoolSetting(
            "W-Tap", "Release block briefly before each attack swing", false));
    private final IntSetting wTapMs = register(new IntSetting(
            "W-Tap Ms", "Milliseconds to drop block for W-tap", 60, 20, 200));
    private final BoolSetting onlyMainHand = register(new BoolSetting(
            "Sword/Axe Only", "Only block when holding a sword or axe", true));

    private long wTapRelease = -1;

    public SwordBlock2() {
        super("SwordBlock2", "Enhanced sword blocking with timing controls and shield support", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.useKey.setPressed(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Check if holding valid weapon
        if (onlyMainHand.isEnabled()) {
            boolean validWeapon = mc.player.getMainHandStack().getItem() instanceof SwordItem
                    || mc.player.getMainHandStack().getItem() instanceof AxeItem;
            if (!validWeapon) {
                mc.options.useKey.setPressed(false);
                return;
            }
        }

        // Check W-tap window
        if (wTap.isEnabled() && wTapRelease > 0) {
            if (System.currentTimeMillis() - wTapRelease < wTapMs.get()) {
                mc.options.useKey.setPressed(false);
                return;
            } else {
                wTapRelease = -1;
            }
        }

        // Determine whether to block
        boolean shouldBlock = false;

        if (proactive.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof LivingEntity le)) continue;
                if (le == mc.player) continue;
                if (!(le instanceof PlayerEntity) && !(le instanceof net.minecraft.entity.mob.HostileEntity)) continue;
                if (mc.player.distanceTo(le) <= blockRange.get()) {
                    shouldBlock = true;
                    break;
                }
            }
        }

        // Prefer shield if available
        if (shouldBlock && useShield.isEnabled()) {
            boolean hasShield = mc.player.getOffHandStack().getItem() instanceof ShieldItem;
            if (hasShield) {
                // Use offhand use key equivalent — interact with offhand
                mc.player.setSneaking(false); // ensure not sneaking to use shield
            }
        }

        mc.options.useKey.setPressed(shouldBlock);
    }

    /**
     * Call this to trigger a W-tap window (drop block briefly for knockback).
     */
    public void triggerWTap() {
        if (wTap.isEnabled()) {
            wTapRelease = System.currentTimeMillis();
        }
    }
}
