package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Set;

public class BlockFilter extends Module {

    private final BoolSetting whitelist = register(new BoolSetting(
            "Whitelist", "If true, only allow mining blocks in the list; otherwise block the list", false));

    private static final Set<Block> FILTER_LIST = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.ANCIENT_DEBRIS, Blocks.BEDROCK
    );

    public BlockFilter() {
        super("BlockFilter", "Prevents mining specific block types based on a configurable list", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        var hit = mc.crosshairTarget;
        if (!(hit instanceof net.minecraft.util.hit.BlockHitResult blockHit)) return;
        Block block = mc.world.getBlockState(blockHit.getBlockPos()).getBlock();
        boolean inList = FILTER_LIST.contains(block);
        boolean shouldBlock = whitelist.isEnabled() ? !inList : inList;
        if (shouldBlock && mc.options.attackKey.isPressed()) {
            mc.options.attackKey.setPressed(false);
        }
    }
}
