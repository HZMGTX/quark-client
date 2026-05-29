package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * AutoBlock — auto right-clicks to block/shield between attacks when an enemy
 * is within range.
 * Mode: Shield (off-hand shield) | Sword (main-hand use).
 */
public class AutoBlock extends Module {

    private final ModeSetting  mode        = register(new ModeSetting ("Mode",         "Blocking mode",                          "Shield", "Shield", "Sword"));
    private final DoubleSetting range       = register(new DoubleSetting("Range",       "Activate when enemy is within this many blocks", 5.0, 1.0, 12.0));
    private final BoolSetting  onlyPlayers = register(new BoolSetting ("Only Players", "Only shield against players",            false));
    private final BoolSetting  onlyBetween = register(new BoolSetting ("Only Between", "Release block while attack CD is < 85%", true));

    public AutoBlock() {
        super("AutoBlock", "Auto right-clicks with shield or sword when enemies are nearby", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.clearActiveItem();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean enemyNear = false;
        double r = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) <= r) { enemyNear = true; break; }
        }

        if (!enemyNear) {
            mc.player.clearActiveItem();
            return;
        }

        // If onlyBetween is on, stop blocking while the attack bar is charging
        if (onlyBetween.isEnabled() && mc.player.getAttackCooldownProgress(0f) < 0.85f) {
            mc.player.clearActiveItem();
            return;
        }

        if (mode.is("Shield")) {
            if (mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            }
        } else { // Sword
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        }
    }
}
