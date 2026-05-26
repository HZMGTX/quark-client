package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ItemStack;

public class AutoSword extends Module {

    private final BoolSetting switchBack = register(new BoolSetting("Switch Back", "Switch back to previous slot on disable", true));
    private int prevSlot = -1;

    public AutoSword() {
        super("AutoSword", "Auto-switches to the best sword when near enemies", Category.PLAYER);
    }

    @Override
    public void onEnable() { prevSlot = -1; }

    @Override
    public void onDisable() {
        if (switchBack.isEnabled() && prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean nearEnemy = false;
        for (var e : mc.world.getEntities()) {
            if (e instanceof LivingEntity le && e != mc.player && mc.player.distanceTo(le) < 5.0) {
                nearEnemy = true;
                break;
            }
        }
        if (!nearEnemy) return;

        int bestSlot = -1;
        float bestDmg = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem sword) {
                float dmg = sword.getMaterial().getAttackDamage();
                if (dmg > bestDmg) { bestDmg = dmg; bestSlot = i; }
            }
        }
        if (bestSlot == -1) return;

        if (prevSlot == -1) prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bestSlot;
    }
}
