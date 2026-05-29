package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * SwordBlock — automatically raises the shield (or sword for old-style
 * blocking) between attacks.  Triggers immediately on incoming damage and
 * between normal attack cooldown windows.
 */
public class SwordBlock extends Module {

    private final BoolSetting   onlySword   = register(new BoolSetting  ("Only Sword",   "Only activate while holding a sword or shield", true));
    private final BoolSetting   damageBlock = register(new BoolSetting  ("Damage Block", "Raise shield immediately on incoming damage",   true));
    private final DoubleSetting holdTime    = register(new DoubleSetting("Hold Time",    "Ms to keep shield raised after damage",          800, 100, 5000));

    private final TimerUtil timer = new TimerUtil();
    private boolean shouldBlock = false;

    public SwordBlock() {
        super("SwordBlock", "Auto-raises shield/sword between attacks and on incoming damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        shouldBlock = false;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.clearActiveItem();
        shouldBlock = false;
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (!damageBlock.isEnabled()) return;
        shouldBlock = true;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Expire block state
        if (shouldBlock && timer.hasReached(holdTime.get())) {
            shouldBlock = false;
        }

        // Also block between attack cooldowns
        boolean cooldownReady = mc.player.getAttackCooldownProgress(0f) >= 1.0f;
        boolean wantBlock = shouldBlock || !cooldownReady;

        // Determine block hand
        Hand blockHand = null;
        if (mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
            blockHand = Hand.OFF_HAND;
        } else if (!onlySword.isEnabled() || mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            blockHand = Hand.MAIN_HAND;
        }

        if (blockHand == null) return;

        if (wantBlock && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, blockHand);
        } else if (!wantBlock && mc.player.isUsingItem()) {
            mc.player.clearActiveItem();
        }
    }
}
