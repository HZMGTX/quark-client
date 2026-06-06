package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

public class SwordBlock extends Module {
    private final BoolSetting autoBlock = register(new BoolSetting("Auto Block", "Automatically hold right-click with sword", true));
    private final BoolSetting onlyWhenEnemy = register(new BoolSetting("Only Enemy", "Only block when enemy nearby", false));

    public SwordBlock() { super("SwordBlock", "Simulates old-style sword blocking", Category.COMBAT); }
    @Override public void onDisable() { mc.options.useKey.setPressed(false); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
            mc.options.useKey.setPressed(false); return;
        }
        if (!autoBlock.isEnabled()) return;
        boolean nearEnemy = false;
        if (onlyWhenEnemy.isEnabled() && mc.world != null) {
            for (var ent : mc.world.getEntities()) {
                if (ent instanceof net.minecraft.entity.LivingEntity le && le != mc.player && mc.player.distanceTo(le) < 6) {
                    nearEnemy = true; break;
                }
            }
        } else nearEnemy = true;
        mc.options.useKey.setPressed(nearEnemy);
    }
}
