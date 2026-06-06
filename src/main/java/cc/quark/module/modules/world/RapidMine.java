package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class RapidMine extends Module {
    private final BoolSetting instantBreak = register(new BoolSetting("Instant", "Break blocks instantly", false));

    public RapidMine() { super("RapidMine", "Increases mining speed significantly", Category.WORLD); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
        if (instantBreak.isEnabled()) {
            mc.interactionManager.attackBlock(bhr.getBlockPos(), bhr.getSide());
            mc.interactionManager.updateBlockBreakingProgress(bhr.getBlockPos(), bhr.getSide());
        }
    }
}
