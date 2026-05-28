package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Vec3d;

public class ItemTracers extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Tracer range in blocks", 50.0, 8.0, 100.0));
    private final BoolSetting rareOnly = register(new BoolSetting("Rare Only", "Only trace uncommon/rare/epic items", false));

    public ItemTracers() {
        super("ItemTracers", "Draws tracer lines to dropped items on the ground", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d from = mc.player.getCameraPosVec(td);
        double maxRange = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ItemEntity ie)) continue;
            double dist = mc.player.distanceTo(ie);
            if (dist > maxRange) continue;

            ItemStack stack = ie.getStack();
            Rarity rarity = stack.getRarity();

            if (rareOnly.isEnabled() && rarity == Rarity.COMMON) continue;

            float r, g, b, a;
            switch (rarity) {
                case UNCOMMON -> { r = 1.0f; g = 1.0f; b = 0.0f; a = 1.0f; }
                case RARE     -> { r = 0.0f; g = 0.6f; b = 1.0f; a = 1.0f; }
                case EPIC     -> { r = 0.6f; g = 0.0f; b = 1.0f; a = 1.0f; }
                default       -> { r = 1.0f; g = 1.0f; b = 1.0f; a = 0.7f; }
            }

            double ex = e.prevX + (e.getX() - e.prevX) * td;
            double ey = e.prevY + (e.getY() - e.prevY) * td;
            double ez = e.prevZ + (e.getZ() - e.prevZ) * td;
            RenderUtil.drawLine3D(m, from, new Vec3d(ex, ey, ez), r, g, b, a, 1.0f);
        }
    }
}
