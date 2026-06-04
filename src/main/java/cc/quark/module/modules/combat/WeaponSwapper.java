package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

import java.util.List;

public class WeaponSwapper extends Module {

    private final BoolSetting preferSword = register(new BoolSetting("Prefer Sword", "Prefer sword over axe", true));
    private final BoolSetting preferAxe = register(new BoolSetting("Prefer Axe", "Prefer axe when sword not available", true));

    public WeaponSwapper() {
        super("WeaponSwapper", "Auto-swaps to best weapon when in combat", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        List<LivingEntity> nearby = EntityUtil.getEntitiesOfType(LivingEntity.class, 5.0);
        nearby.removeIf(e -> e == mc.player);

        if (nearby.isEmpty()) return;

        int bestSlot = -1;
        boolean foundSword = false;
        boolean foundAxe = false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof SwordItem) {
                if (preferSword.isEnabled() && !foundSword) {
                    bestSlot = i;
                    foundSword = true;
                }
            } else if (stack.getItem() instanceof AxeItem) {
                if (preferAxe.isEnabled() && !foundSword && !foundAxe) {
                    bestSlot = i;
                    foundAxe = true;
                }
            }
        }

        if (bestSlot != -1 && mc.player.getInventory().selectedSlot != bestSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
