package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class FakePlayer extends Module {

    private final BoolSetting showESP = register(new BoolSetting("Show ESP", "Draw ESP box at spawn position", true));

    private double spawnX, spawnY, spawnZ;
    private float  spawnYaw;

    public FakePlayer() {
        super("FakePlayer", "Marks your spawn position with an ESP ghost for bait or tracking", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        spawnX   = mc.player.getX();
        spawnY   = mc.player.getY();
        spawnZ   = mc.player.getZ();
        spawnYaw = mc.player.getYaw();
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        double dist = mc.player.getPos().distanceTo(new Vec3d(spawnX, spawnY, spawnZ));
        return String.format("%.0fm", dist);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (!showESP.isEnabled() || mc.player == null) return;
        Box box = new Box(spawnX - 0.3, spawnY, spawnZ - 0.3, spawnX + 0.3, spawnY + 1.8, spawnZ + 0.3);
        RenderUtil.drawESPBox(event.getMatrixStack(), box, 0.2f, 0.8f, 1.0f, 0.9f, 1.5f);
        RenderUtil.drawFilledBox(event.getMatrixStack(), box, 0.2f, 0.8f, 1.0f, 0.15f);
    }
}
