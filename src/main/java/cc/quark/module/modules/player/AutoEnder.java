package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoEnder extends Module {
    private final DoubleSetting health = register(new DoubleSetting("HP Threshold","Throw ender pearl below this HP",6.0,1.0,20.0));
    private final TimerUtil timer = new TimerUtil();
    public AutoEnder() { super("AutoEnder","Throws an ender pearl when HP is critical",Category.PLAYER); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.world==null||mc.interactionManager==null) return;
        if (mc.player.getHealth() > health.get()) return;
        if (!timer.hasReached(2000)) return;
        for (int i=0;i<9;i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isOf(Items.ENDER_PEARL)) continue;
            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = i;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            break;
        }
    }
}
