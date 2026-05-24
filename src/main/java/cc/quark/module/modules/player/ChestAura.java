package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to search chests", 4.0, 1.0, 6.0));
    private final IntSetting delay = register(new IntSetting("Steal Delay", "Ticks between steals", 5, 1, 20));
    private final BoolSetting closeAfter = register(new BoolSetting("Close After", "Close chest after stealing", true));

    private final TimerUtil timer = new TimerUtil();
    private BlockPos targetChest = null;
    private boolean stealing = false;
    private int slotIndex = 0;

    public ChestAura() {
        super("ChestAura", "Automatically steals from nearby chests", Category.PLAYER, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
        targetChest = null;
        stealing = false;
        slotIndex = 0;
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
        if (closeAfter.getValue() && mc.currentScreen instanceof HandledScreen) {
            mc.player.closeHandledScreen();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (stealing && mc.currentScreen instanceof HandledScreen<?> screen) {
            if (!timer.hasReached(delay.getValue() * 50L)) return;
            int containerSize = screen.getScreenHandler().slots.size() - 36;
            if (slotIndex < containerSize) {
                mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, slotIndex, 0, SlotActionType.QUICK_MOVE, mc.player);
                slotIndex++;
                timer.reset();
            } else {
                if (closeAfter.getValue()) mc.player.closeHandledScreen();
                stealing = false;
                targetChest = null;
                slotIndex = 0;
            }
            return;
        }

        if (stealing) return;

        BlockPos playerPos = mc.player.getBlockPos();
        double r = range.getValue();
        for (BlockPos pos : BlockPos.iterate(
                (int)(playerPos.getX()-r), (int)(playerPos.getY()-r), (int)(playerPos.getZ()-r),
                (int)(playerPos.getX()+r), (int)(playerPos.getY()+r), (int)(playerPos.getZ()+r))) {
            if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > r) continue;
            BlockEntity be = mc.world.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false));
                targetChest = pos.toImmutable();
                stealing = true;
                slotIndex = 0;
                timer.reset();
                return;
            }
        }
    }
}
