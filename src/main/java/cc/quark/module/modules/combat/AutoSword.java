package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

import java.util.List;

public class AutoSword extends Module {

    private final BoolSetting preferAxe = register(new BoolSetting("Prefer Axe", "Prefer axe over sword", false));
    private final IntSetting range = register(new IntSetting("Range", "Range to detect enemies", 5, 1, 10));

    public AutoSword() {
        super("AutoSword", "Auto-switches to sword/axe when entering combat", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player || !(e instanceof PlayerEntity));

        if (targets.isEmpty()) return;

        int swordSlot = -1;
        int axeSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof SwordItem && swordSlot == -1) swordSlot = i;
            if (stack.getItem() instanceof AxeItem && axeSlot == -1) axeSlot = i;
        }

        int targetSlot = -1;
        if (preferAxe.isEnabled()) {
            targetSlot = axeSlot != -1 ? axeSlot : swordSlot;
        } else {
            targetSlot = swordSlot != -1 ? swordSlot : axeSlot;
        }

        if (targetSlot != -1 && mc.player.getInventory().selectedSlot != targetSlot) {
            mc.player.getInventory().selectedSlot = targetSlot;
        }
    }
}
