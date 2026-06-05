package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;

public class MobESP extends Module {
    private final BoolSetting hostile = register(new BoolSetting("Hostile", "Show hostile mobs", true));
    private final BoolSetting passive = register(new BoolSetting("Passive", "Show passive mobs", false));
    private final IntSetting range = register(new IntSetting("Range", "Detection range", 32, 8, 128));

    public MobESP() { super("MobESP", "ESP for mobs - shows hostile and passive mobs", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.world == null) return;
        MatrixStack ms = e.getMatrixStack();
        for (var ent : mc.world.getEntities()) {
            if (mc.player.distanceTo(ent) > range.get()) continue;
            if (hostile.isEnabled() && ent instanceof HostileEntity he) {
                RenderUtil.drawESPBox(ms, he.getBoundingBox(), 1f, 0.2f, 0.2f, 0.8f, 1.5f);
            } else if (passive.isEnabled() && ent instanceof PassiveEntity pe) {
                RenderUtil.drawESPBox(ms, pe.getBoundingBox(), 0.2f, 1f, 0.2f, 0.8f, 1.5f);
            }
        }
    }
}
