package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TrapDetector extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to scan for trap blocks", 8.0, 2.0, 20.0));

    private final BoolSetting showHUD = register(new BoolSetting(
            "Show HUD", "Show trap warning on screen", true));

    private final List<BlockPos> trapBlocks = new ArrayList<>();
    private boolean trapDetected = false;

    public TrapDetector() {
        super("TrapDetector", "Detects and warns about enemy traps", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        trapBlocks.clear();
        trapDetected = false;

        int r = (int) range.get();
        BlockPos origin = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.TNT || block == Blocks.OBSIDIAN
                            || block == Blocks.BEDROCK || block == Blocks.SAND
                            || block == Blocks.GRAVEL) {
                        trapBlocks.add(pos);
                        trapDetected = true;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || !trapDetected) return;
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        ctx.drawCenteredTextWithShadow(
                mc.textRenderer,
                "TRAP DETECTED (" + trapBlocks.size() + " blocks)",
                sw / 2,
                40,
                0xFFFF5555
        );
    }
}
