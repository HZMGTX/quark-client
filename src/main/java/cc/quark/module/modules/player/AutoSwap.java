package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;

public class AutoSwap extends Module {

    private final DoubleSetting combatRange = register(new DoubleSetting(
            "Combat Range", "Switch to weapon when enemy is this close", 5.0, 2.0, 10.0));
    private final BoolSetting preferAxe = register(new BoolSetting(
            "Prefer Axe", "Prefer axe over sword when both are in hotbar", false));
    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Return to previous slot when out of combat", true));

    private int savedSlot = 0;
    private boolean inCombat = false;

    public AutoSwap() {
        super("AutoSwap", "Equips the best weapon automatically when enemies are nearby", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean enemyNear = false;
        for (net.minecraft.entity.Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le) || le.isDead()) continue;
            if (mc.player.distanceTo(e) <= combatRange.get()) { enemyNear = true; break; }
        }

        if (enemyNear && !inCombat) {
            inCombat  = true;
            savedSlot = mc.player.getInventory().selectedSlot;
            switchToWeapon();
        } else if (!enemyNear && inCombat) {
            inCombat = false;
            if (restoreSlot.isEnabled()) mc.player.getInventory().selectedSlot = savedSlot;
        }
    }

    private void switchToWeapon() {
        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            boolean match = preferAxe.isEnabled()
                    ? (item instanceof AxeItem || item instanceof SwordItem)
                    : (item instanceof SwordItem || item instanceof AxeItem);
            if (match) { mc.player.getInventory().selectedSlot = i; return; }
        }
    }
}
