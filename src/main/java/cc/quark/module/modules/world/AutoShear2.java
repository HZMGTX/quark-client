package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoShear2 extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to search for sheep", 8, 1, 16));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between shear attempts", 200, 50, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoShear2() {
        super("AutoShear2", "Shears all sheep in range aggressively", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        int shearSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.SHEARS) {
                shearSlot = i;
                break;
            }
        }
        if (shearSlot == -1) return;

        double r = radius.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof SheepEntity sheep)) continue;
            if (mc.player.distanceTo(sheep) > r) continue;
            if (sheep.isSheared() || sheep.isBaby()) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(shearSlot));
            mc.player.getInventory().selectedSlot = shearSlot;
            mc.interactionManager.interactEntity(mc.player, sheep, Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
            mc.player.getInventory().selectedSlot = saved;
            return;
        }
    }
}
