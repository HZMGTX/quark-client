package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.hit.HitResult;

public class SnapLook extends Module {

    private final BoolSetting onCrosshair = register(new BoolSetting("On Crosshair", "Only snap pitch level", true));

    public SnapLook() {
        super("SnapLook", "Snaps player pitch flat when targeting a block", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (!onCrosshair.isEnabled()) return;
        if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            mc.player.setPitch(0f);
        }
    }
}
