package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoCrossbow extends Module {
    private final IntSetting range = register(new IntSetting("Range", "Auto-fire range", 10, 1, 30));
    private int state = 0; // 0=idle, 1=loading, 2=loaded
    private int loadTicks = 0;

    public AutoCrossbow() { super("AutoCrossbow", "Auto-loads and fires crossbow at enemies", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); state = 0; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof CrossbowItem)) return;

        boolean loaded = CrossbowItem.isCharged(held);
        if (!loaded && state != 1) {
            mc.options.useKey.setPressed(true);
            state = 1; loadTicks = 0;
        } else if (state == 1) {
            loadTicks++;
            if (loadTicks > 25) { mc.options.useKey.setPressed(false); state = 2; }
        } else if (loaded) {
            state = 2;
            PlayerEntity target = null;
            double closest = range.get();
            for (var ent : mc.world.getEntities()) {
                if (!(ent instanceof PlayerEntity pe) || pe == mc.player) continue;
                double d = mc.player.distanceTo(pe);
                if (d < closest) { closest = d; target = pe; }
            }
            if (target != null) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                state = 0;
            }
        }
    }
}
