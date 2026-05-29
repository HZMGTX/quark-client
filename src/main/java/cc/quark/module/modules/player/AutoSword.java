package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.entity.LivingEntity;

public class AutoSword extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Switch to sword when enemy within this range", 5.0, 2.0, 12.0));
    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Restore previous slot when enemies leave", true));

    private int prevSlot = -1;
    private boolean hadEnemy = false;

    public AutoSword() {
        super("AutoSword", "Auto-switches to best sword when enemies are nearby", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
        hadEnemy = false;
    }

    @Override
    public void onDisable() {
        if (switchBack.isEnabled() && prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean enemyNear = false;
        for (var e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le) || le.isDead()) continue;
            if (mc.player.distanceTo(le) <= range.get()) {
                enemyNear = true;
                break;
            }
        }

        if (enemyNear && !hadEnemy) {
            hadEnemy = true;
            int swordSlot = InventoryUtil.findBestSword();
            if (swordSlot >= 0 && swordSlot < 9) {
                if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = swordSlot;
            }
        } else if (!enemyNear && hadEnemy) {
            hadEnemy = false;
            if (switchBack.isEnabled() && prevSlot >= 0) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
        }
    }
}
