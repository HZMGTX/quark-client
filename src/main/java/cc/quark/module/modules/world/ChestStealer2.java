package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer2 extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between steal clicks", 80, 20, 500));
    private final BoolSetting filterJunk = register(new BoolSetting(
            "Filter Junk", "Skip worthless items like dirt, gravel, rotten flesh", true));
    private final BoolSetting closeAfter = register(new BoolSetting(
            "Close After", "Close the container when done stealing", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean done = false;

    public ChestStealer2() {
        super("ChestStealer2", "Improved chest stealer: steals items from open containers with junk filter", Category.WORLD);
    }

    @Override
    public void onEnable() {
        done = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?>)) {
            done = false;
            return;
        }
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        int containerSize = handler.slots.size() - 36;
        if (containerSize <= 0) return;

        for (int i = 0; i < containerSize; i++) {
            Slot slot = handler.slots.get(i);
            if (!slot.hasStack()) continue;
            if (!slot.canTakeItems(mc.player)) continue;
            if (filterJunk.isEnabled() && isJunk(slot)) continue;

            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        // All done
        if (closeAfter.isEnabled() && !done) {
            done = true;
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
            mc.currentScreen.close();
        }
    }

    private boolean isJunk(Slot slot) {
        var item = slot.getStack().getItem();
        return item == Items.DIRT || item == Items.GRAVEL || item == Items.SAND
                || item == Items.ROTTEN_FLESH || item == Items.BONE
                || item == Items.ARROW || item == Items.COBBLESTONE
                || item == Items.NETHERRACK || item == Items.FLINT;
    }
}
