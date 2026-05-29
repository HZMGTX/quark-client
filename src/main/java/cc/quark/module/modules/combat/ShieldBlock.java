package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * ShieldBlock - automatically right-clicks a shield when the player is about to take
 * damage or when an enemy attack is detected. Lowers the shield after a configurable
 * number of ticks.
 */
public class ShieldBlock extends Module {

    private final BoolSetting onDamage = register(new BoolSetting(
            "On Damage", "Block when receiving damage", true));

    private final BoolSetting onAttack = register(new BoolSetting(
            "On Enemy Attack", "Block when an enemy attacks us (EventAttack cancel detection)", false));

    private final IntSetting holdTicks = register(new IntSetting(
            "Hold Ticks", "How many ticks to hold the shield up after activating", 3, 1, 20));

    private final BoolSetting requireOffhand = register(new BoolSetting(
            "Offhand Only", "Only block if shield is in offhand", true));

    private int ticksBlocking = 0;
    private boolean blocking = false;

    public ShieldBlock() {
        super("ShieldBlock", "Auto right-clicks shield when taking damage to block incoming hits", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticksBlocking = 0;
        blocking = false;
    }

    @Override
    public void onDisable() {
        if (blocking && mc.player != null) {
            mc.player.stopUsingItem();
            blocking = false;
        }
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (!onDamage.isEnabled()) return;
        activateShield();
    }

    @EventHandler
    public void onAttacked(EventAttack event) {
        if (!onAttack.isEnabled()) return;
        // If we ourselves are attacking, skip — this is for when we're the victim
        // EventAttack fires when our player attacks something, so filter it
        if (event.getTarget() == mc.player) {
            activateShield();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (blocking) {
            ticksBlocking++;
            if (ticksBlocking >= holdTicks.get()) {
                mc.player.stopUsingItem();
                blocking = false;
                ticksBlocking = 0;
            }
        }
    }

    private void activateShield() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (blocking) return; // already blocking

        Hand shieldHand = findShieldHand();
        if (shieldHand == null) return;

        mc.interactionManager.interactItem(mc.player, shieldHand);
        blocking = true;
        ticksBlocking = 0;
    }

    private Hand findShieldHand() {
        if (requireOffhand.isEnabled()) {
            ItemStack offhand = mc.player.getOffHandStack();
            if (offhand.isOf(Items.SHIELD)) return Hand.OFF_HAND;
            return null;
        }
        // Check offhand first, then main hand
        if (mc.player.getOffHandStack().isOf(Items.SHIELD))   return Hand.OFF_HAND;
        if (mc.player.getMainHandStack().isOf(Items.SHIELD))  return Hand.MAIN_HAND;
        return null;
    }
}
