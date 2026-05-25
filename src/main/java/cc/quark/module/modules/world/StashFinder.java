package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class StashFinder extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius in blocks", 32, 8, 64));
    private final BoolSetting announceNew = register(new BoolSetting(
            "Announce", "Print in chat when new containers are found", true));

    private final List<BlockPos> found = new ArrayList<>();
    private int scanTicker = 0;

    public StashFinder() {
        super("StashFinder", "Highlights nearby storage containers — great for finding stashes", Category.WORLD);
    }

    @Override
    public void onEnable() { found.clear(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (++scanTicker < 20) return;
        scanTicker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        List<BlockPos> newFound = new ArrayList<>();

        for (BlockPos pos : BlockPos.iterate(center.add(-r,-r,-r), center.add(r,r,r))) {
            BlockEntity be = mc.world.getBlockEntity(pos);
            if (be instanceof LootableContainerBlockEntity) {
                newFound.add(pos.toImmutable());
            }
        }

        if (announceNew.isEnabled() && newFound.size() != found.size()) {
            ChatUtil.info("StashFinder: " + newFound.size() + " container(s) nearby.");
        }
        found.clear();
        found.addAll(newFound);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (found.isEmpty()) return;
        MatrixStack matrices = event.getMatrixStack();
        for (BlockPos pos : found) {
            Box box = new Box(pos);
            RenderUtil.drawESPBox(matrices, box, 0.9f, 0.65f, 0.1f, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(matrices, box, 0.9f, 0.65f, 0.1f, 0.12f);
        }
    }
}
