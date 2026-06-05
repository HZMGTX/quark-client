package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class AutoSmeltFarm extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Search radius for furnaces", 5.0, 1.0, 10.0));
    private final BoolSetting alertFound = register(new BoolSetting("Alert", "Show furnace count in chat", true));
    private final TimerUtil timer = new TimerUtil();

    public AutoSmeltFarm() {
        super("Auto Smelt Farm", "Detects nearby furnaces for smelt farming", Category.WORLD, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(5000)) return;
        timer.reset();

        int furnaceCount = 0;
        int r = (int) Math.ceil(range.get());
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    var block = mc.world.getBlockState(playerPos.add(x, y, z)).getBlock();
                    if (block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER) furnaceCount++;
                }
            }
        }
        if (alertFound.isEnabled() && furnaceCount > 0) {
            ChatUtil.info("[SmeltFarm] §f" + furnaceCount + " §7furnace(s) nearby.");
        }
    }
}
