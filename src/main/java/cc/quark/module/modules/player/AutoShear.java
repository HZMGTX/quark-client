package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.List;

public class AutoShear extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between shearing attempts", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoShear() {
        super("AutoShear", "Automatically shears nearby sheep when holding shears", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Check if player has shears in hotbar
        int shearSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.SHEARS) {
                shearSlot = i;
                break;
            }
        }
        if (shearSlot == -1) return;

        // Switch to shears and notify server of slot change
        mc.player.getInventory().selectedSlot = shearSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(shearSlot));

        // Find nearby unsheared sheep
        Box searchBox = mc.player.getBoundingBox().expand(5.0);
        List<SheepEntity> sheep = mc.world.getEntitiesByClass(SheepEntity.class, searchBox,
                s -> !s.isSheared() && s.distanceTo(mc.player) <= 5.0);

        if (sheep.isEmpty()) return;

        SheepEntity target = sheep.get(0);
        mc.interactionManager.interactEntity(mc.player, target, Hand.MAIN_HAND);
        timer.reset();
    }
}
