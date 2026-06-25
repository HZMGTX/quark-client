package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * SmartBlock - automatically shields or sword-blocks at the optimal moment
 * when an enemy is about to attack.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Shield</b>  - activates the shield (right-click with shield in offhand)
 *                        when an enemy within range has full attack cooldown.</li>
 *   <li><b>Sword</b>   - right-click blocks with the sword when the enemy is close.</li>
 *   <li><b>Auto</b>    - prefers shield if available, otherwise sword-blocks.</li>
 * </ul>
 */
public class SmartBlock extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Blocking mode", "Auto", "Auto", "Shield", "Sword"));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Distance to activate block (blocks)", 4.0, 1.0, 8.0));

    private final DoubleSetting cooldownThreshold = register(new DoubleSetting(
            "Enemy Cooldown %", "Enemy attack cooldown must be above this to trigger block", 85.0, 0.0, 100.0));

    private final BoolSetting autoRelease = register(new BoolSetting(
            "Auto Release", "Automatically release block when enemy attack cooldown resets", true));

    private boolean blocking = false;

    public SmartBlock() {
        super("SmartBlock", "Auto-shields or blocks at optimal timing when enemy attacks", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (blocking && mc.player != null && mc.player.isUsingItem()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        blocking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        PlayerEntity threat = findNearestThreat();

        if (threat != null) {
            // Check if the enemy's cooldown is high enough that an attack is imminent
            float enemyCooldown = threat.getAttackCooldownProgress(0.0f);
            if (enemyCooldown >= (cooldownThreshold.get() / 100.0)) {
                if (!blocking) {
                    activateBlock();
                    blocking = true;
                }
                return;
            }
        }

        // No threat or cooldown too low — release block
        if (blocking && autoRelease.isEnabled()) {
            if (mc.player.isUsingItem()) {
                mc.interactionManager.stopUsingItem(mc.player);
            }
            blocking = false;
        }
    }

    private void activateBlock() {
        String m = mode.get();
        boolean hasShield = mc.player.getOffHandStack().getItem() instanceof ShieldItem;
        boolean hasSword  = mc.player.getMainHandStack().getItem() instanceof SwordItem;

        if ((m.equals("Shield") || (m.equals("Auto") && hasShield)) && hasShield) {
            if (!mc.player.isUsingItem()) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            }
        } else if ((m.equals("Sword") || m.equals("Auto")) && hasSword) {
            if (!mc.player.isUsingItem()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        }
    }

    private PlayerEntity findNearestThreat() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
