package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoLeash extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to search for passive mobs", 8, 1, 16));

    private final TimerUtil timer = new TimerUtil();

    public AutoLeash() {
        super("AutoLeash", "Leashes nearby passive mobs with lead from inventory", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;
        timer.reset();

        int leadSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.LEAD) {
                leadSlot = i;
                break;
            }
        }
        if (leadSlot == -1) return;

        double r = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (mc.player.distanceTo(animal) > r) continue;
            if (animal.isLeashed()) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(leadSlot));
            mc.player.getInventory().selectedSlot = leadSlot;
            mc.interactionManager.interactEntity(mc.player, animal, Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
            mc.player.getInventory().selectedSlot = saved;
            return;
        }
    }
}
