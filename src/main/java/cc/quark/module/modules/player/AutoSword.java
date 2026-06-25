package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;

public class AutoSword extends Module {

    private final BoolSetting allowAxe = register(new BoolSetting("AllowAxe", "AllowAxe", true));

    public AutoSword() {
        super("AutoSword", "Automatically switches to sword/axe when a mob is nearby", Category.PLAYER);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
        if (mc == null || mc.player == null || mc.world == null) return;

        boolean enemyNear = !mc.world.getEntitiesByClass(
            LivingEntity.class,
            mc.player.getBoundingBox().expand(5),
            e -> !e.equals(mc.player) && e.isAlive()
        ).isEmpty();

        if (!enemyNear) return;

        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            var item = inv.getStack(i).getItem();
            if (item instanceof SwordItem || (allowAxe.isEnabled() && item instanceof AxeItem)) {
                if (inv.selectedSlot != i) inv.selectedSlot = i;
                return;
            }
        }
    }
}
