package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;

public class AutoSit extends Module {

    private final BoolSetting onSlab = register(new BoolSetting(
            "OnSlab", "Auto-sneak when standing on a slab", true));
    private final BoolSetting onStair = register(new BoolSetting(
            "OnStair", "Auto-sneak when standing on a stair", true));

    public AutoSit() {
        super("AutoSit", "Automatically sneaks to sit on slabs and stairs", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        var blockState = mc.world.getBlockState(below);
        var block = blockState.getBlock();

        boolean isOnSlab = onSlab.isEnabled() && block instanceof SlabBlock;
        boolean isOnStair = onStair.isEnabled() && block instanceof StairsBlock;

        if (isOnSlab || isOnStair) {
            mc.player.setSneaking(true);
            mc.options.sneakKey.setPressed(true);
        } else {
            if (mc.options.sneakKey.isPressed()) {
                mc.options.sneakKey.setPressed(false);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSneaking(false);
            mc.options.sneakKey.setPressed(false);
        }
    }
}
