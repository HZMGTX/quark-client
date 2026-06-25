package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoMine extends Module {

    private final IntSetting reach = register(new IntSetting(
            "Reach", "Maximum block reach distance (blocks)", 5, 1, 10));

    private final BoolSetting onlyOneBlock = register(new BoolSetting(
            "OnlyOneBlock", "Stop mining after one block is broken", false));

    private boolean brokeBlock = false;

    public AutoMine() {
        super("AutoMine", "Automatically mines blocks the crosshair points at", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        brokeBlock = false;
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.attackKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (onlyOneBlock.isEnabled() && brokeBlock) return;

        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (hit.getType() != HitResult.Type.BLOCK) return;

        double dist = mc.player.getEyePos().distanceTo(blockHit.getPos());
        if (dist > reach.get()) return;

        // Simulate holding the attack key to continuously mine
        mc.options.attackKey.setPressed(true);

        // Check if the block was just destroyed (no longer present)
        if (mc.world.getBlockState(blockHit.getBlockPos()).isAir()) {
            brokeBlock = true;
            if (onlyOneBlock.isEnabled()) {
                mc.options.attackKey.setPressed(false);
            }
        }
    }
}
