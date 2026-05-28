package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class Chams extends Module {

    private final BoolSetting players   = register(new BoolSetting("Players",  "Highlight players",      true));
    private final BoolSetting mobs      = register(new BoolSetting("Mobs",     "Highlight hostile mobs", false));
    private final BoolSetting animals   = register(new BoolSetting("Animals",  "Highlight animals",      false));
    private final BoolSetting selfChams = register(new BoolSetting("Self",     "Highlight yourself",     false));
    private final ModeSetting mode      = register(new ModeSetting("Mode", "Render mode", "Fill", "Fill", "Outline", "Both"));
    private final ColorSetting playerColor = register(new ColorSetting("Player Color", "Color for players", 0x554444FF));
    private final ColorSetting mobColor    = register(new ColorSetting("Mob Color",    "Color for mobs",    0x55FF4444));
    private final ColorSetting animalColor = register(new ColorSetting("Animal Color", "Color for animals", 0x5544FF44));
    private final IntSetting   alpha       = register(new IntSetting("Alpha", "Fill transparency", 60, 10, 200));

    public Chams() {
        super("Chams", "Makes entities visible through walls with colored fills/outlines", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player && !selfChams.isEnabled()) continue;
            if (!living.isAlive()) continue;

            int argb;
            if (living instanceof PlayerEntity) {
                if (!players.isEnabled()) continue;
                argb = playerColor.get();
            } else if (living instanceof HostileEntity) {
                if (!mobs.isEnabled()) continue;
                argb = mobColor.get();
            } else if (living instanceof AnimalEntity) {
                if (!animals.isEnabled()) continue;
                argb = animalColor.get();
            } else continue;

            int a = alpha.get();
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8)  & 0xFF) / 255f;
            float b = (argb         & 0xFF) / 255f;
            float af = a / 255f;

            Box box = living.getBoundingBox();
            String m = mode.get();
            if (m.equals("Fill") || m.equals("Both")) {
                RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, af * 0.5f);
            }
            if (m.equals("Outline") || m.equals("Both")) {
                RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, Math.min(1f, af * 2), 1.5f);
            }
        }
    }
}
