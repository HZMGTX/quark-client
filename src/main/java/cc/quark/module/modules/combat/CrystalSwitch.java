package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * CrystalSwitch — immediately attacks newly placed end crystals with the best sword.
 */
public class CrystalSwitch extends Module {

    private final BoolSetting switchBack = register(new BoolSetting(
            "SwitchBack", "Switch back to the previous slot after attacking", true));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum range to attack crystals", 6.0, 1.0, 10.0));

    public CrystalSwitch() {
        super("CrystalSwitch", "Instantly attacks end crystals when they are placed", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (mc.player.distanceTo(crystal) > range.get()) continue;

            int swordSlot = findBestSwordSlot();
            if (swordSlot == -1) {
                // Still attack with current item
                mc.interactionManager.attackEntity(mc.player, crystal);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = swordSlot;

            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (switchBack.isEnabled()) {
                mc.player.getInventory().selectedSlot = prevSlot;
            }
            // Only attack one crystal per tick to avoid spam
            return;
        }
    }

    private int findBestSwordSlot() {
        int bestSlot = -1;
        int bestDamage = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof SwordItem sword)) continue;
            int dmg = (int) sword.getMaterial().value().attackDamageBonus();
            if (dmg > bestDamage) {
                bestDamage = dmg;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
