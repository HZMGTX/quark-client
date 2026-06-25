package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ChestLocker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for chests to mark", 3.0, 1.0, 6.0));
    private final BoolSetting showMarked = register(new BoolSetting(
            "ShowMarked", "Show count of marked chests in chat", true));

    private final Set<BlockPos> markedChests = new HashSet<>();

    public ChestLocker() {
        super("ChestLocker", "Marks nearby chests as personal — warns if an unmarked chest is opened", Category.WORLD);
    }

    @Override
    public void onEnable() {
        markedChests.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var be = mc.world.getBlockEntity(pos);
            if (!(be instanceof ChestBlockEntity) && !(be instanceof ShulkerBoxBlockEntity)) continue;

            BlockPos immutable = pos.toImmutable();
            if (!markedChests.contains(immutable)) {
                markedChests.add(immutable);
                if (showMarked.isEnabled()) {
                    ChatUtil.info("[ChestLocker] Marked chest at " + immutable.getX()
                            + " " + immutable.getY() + " " + immutable.getZ()
                            + " (" + markedChests.size() + " total)");
                }
            }
        }

        // Warn if the currently open screen is not a marked chest
        if (mc.currentScreen instanceof GenericContainerScreen) {
            boolean openingKnown = false;
            for (BlockPos marked : markedChests) {
                double d = mc.player.squaredDistanceTo(marked.getX(), marked.getY(), marked.getZ());
                if (d <= rangeSq + 1) {
                    openingKnown = true;
                    break;
                }
            }
            if (!openingKnown) {
                ChatUtil.warn("[ChestLocker] Opening an unknown/unmarked chest!");
            }
        }
    }
}
