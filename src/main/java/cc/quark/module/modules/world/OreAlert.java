package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class OreAlert extends Module {

    private final IntSetting range   = register(new IntSetting("Range", "Detection radius", 12, 4, 24));
    private final BoolSetting diamond = register(new BoolSetting("Diamond",       "Alert on diamond ore",    true));
    private final BoolSetting ancient = register(new BoolSetting("Ancient Debris","Alert on ancient debris", true));
    private final BoolSetting emerald = register(new BoolSetting("Emerald",       "Alert on emerald ore",    false));
    private final BoolSetting gold    = register(new BoolSetting("Gold",          "Alert on gold ore",       false));

    private int ticker = 0;
    private int prevCount = 0;

    public OreAlert() {
        super("OreAlert", "Alerts in chat when rare ores are detected nearby", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (++ticker < 40) return;
        ticker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        int count = 0;

        for (BlockPos pos : BlockPos.iterate(center.add(-r,-r,-r), center.add(r,r,r))) {
            var s = mc.world.getBlockState(pos);
            if (diamond.isEnabled() && (s.isOf(Blocks.DIAMOND_ORE) || s.isOf(Blocks.DEEPSLATE_DIAMOND_ORE))) count++;
            if (ancient.isEnabled() &&  s.isOf(Blocks.ANCIENT_DEBRIS))                                       count++;
            if (emerald.isEnabled() && (s.isOf(Blocks.EMERALD_ORE) || s.isOf(Blocks.DEEPSLATE_EMERALD_ORE))) count++;
            if (gold.isEnabled()    && (s.isOf(Blocks.GOLD_ORE)    || s.isOf(Blocks.DEEPSLATE_GOLD_ORE) || s.isOf(Blocks.NETHER_GOLD_ORE))) count++;
        }

        if (count > prevCount) {
            ChatUtil.info("OreAlert: " + count + " rare ore(s) within " + r + " blocks!");
        }
        prevCount = count;
    }
}
