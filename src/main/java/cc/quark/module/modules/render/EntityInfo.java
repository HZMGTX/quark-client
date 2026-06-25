package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityInfo extends Module {
    private final BoolSetting showHealth = register(new BoolSetting("Health", "Show entity health", true));
    private final BoolSetting showName = register(new BoolSetting("Name", "Show entity name", true));
    private final BoolSetting showDist = register(new BoolSetting("Distance", "Show distance to entity", false));
    private final IntSetting range = register(new IntSetting("Range", "Range to show info", 20, 1, 64));

    public EntityInfo() { super("EntityInfo", "Shows health and info tags above entities", Category.RENDER); }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.world == null) return;
        MatrixStack ms = e.getMatrixStack();
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof LivingEntity le) || le == mc.player) continue;
            if (mc.player.distanceTo(le) > range.get()) continue;

            StringBuilder info = new StringBuilder();
            if (showName.isEnabled()) info.append(le.getName().getString());
            if (showHealth.isEnabled()) info.append(" §c").append((int)le.getHealth()).append("❤");
            if (showDist.isEnabled()) info.append(" §7").append((int)mc.player.distanceTo(le)).append("m");
        }
    }
}
