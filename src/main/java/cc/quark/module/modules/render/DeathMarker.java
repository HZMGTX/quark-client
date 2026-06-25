package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class DeathMarker extends Module {

    private final IntSetting  posX     = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY     = register(new IntSetting("Y", "HUD Y position", 100, 0, 3000));
    private final BoolSetting showDist = register(new BoolSetting("Show Distance", "Show distance to death point", true));
    private final BoolSetting autoClear = register(new BoolSetting("Auto Clear", "Clear marker on dimension change", true));

    private BlockPos deathPos = null;
    private String deathDim = null;
    private boolean wasDead = false;

    public DeathMarker() {
        super("DeathMarker", "Marks death location on the HUD and shows distance/coords", Category.RENDER);
    }

    @Override
    public void onEnable() {
        deathPos = null;
        deathDim = null;
        wasDead = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean dead = mc.player.getHealth() <= 0f;

        if (dead && !wasDead) {
            deathPos = mc.player.getBlockPos();
            deathDim = mc.world.getRegistryKey().getValue().toString();
            wasDead = true;
        }

        if (!dead) {
            wasDead = false;
        }

        // Clear on dimension change
        if (autoClear.isEnabled() && deathDim != null) {
            String current = mc.world.getRegistryKey().getValue().toString();
            if (!current.equals(deathDim)) {
                deathPos = null;
                deathDim = null;
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || deathPos == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("§cDeath: §f%d, %d, %d", deathPos.getX(), deathPos.getY(), deathPos.getZ()),
                x, y, 0xFFFFFFFF);

        if (showDist.isEnabled()) {
            double dx = mc.player.getX() - deathPos.getX();
            double dy = mc.player.getY() - deathPos.getY();
            double dz = mc.player.getZ() - deathPos.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            ctx.drawTextWithShadow(mc.textRenderer,
                    String.format("§7Distance: §f%.1f", dist),
                    x, y + lh, 0xFFAAAAAA);
        }
    }
}
