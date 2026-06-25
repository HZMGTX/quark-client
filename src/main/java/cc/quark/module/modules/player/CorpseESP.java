package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CorpseESP extends Module {

    private final IntSetting maxMarkers = register(new IntSetting(
            "MaxMarkers", "Maximum number of death markers to track", 10, 1, 50));

    private final List<Vec3d> deathPositions = new ArrayList<>();

    public CorpseESP() {
        super("CorpseESP", "Marks the positions where entities died in the world", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        deathPositions.clear();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.world == null) return;
        if (!(event.getPacket() instanceof EntityStatusS2CPacket packet)) return;

        // Status 3 = entity death
        if (packet.getStatus() != 3) return;

        Entity entity = packet.getEntity(mc.world);
        if (!(entity instanceof LivingEntity)) return;

        Vec3d pos = entity.getPos();
        mc.execute(() -> {
            if (deathPositions.size() >= maxMarkers.get()) {
                deathPositions.remove(0);
            }
            deathPositions.add(pos);
        });
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (deathPositions.isEmpty()) return;

        MatrixStack matrices = event.getMatrixStack();
        for (Vec3d pos : deathPositions) {
            Box box = new Box(pos.x - 0.4, pos.y, pos.z - 0.4,
                    pos.x + 0.4, pos.y + 1.8, pos.z + 0.4);
            RenderUtil.drawESPBox(matrices, box, 1.0f, 0.0f, 0.0f, 0.9f, 2.0f);
            RenderUtil.drawFilledBox(matrices, box, 1.0f, 0.0f, 0.0f, 0.15f);
        }
    }
}
