package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.SoundEvents;

public class TotemAlert extends Module {

    private final ModeSetting sound = register(new ModeSetting(
            "Sound", "Sound to play when a nearby player pops a totem", "Ding",
            "Ding", "Explode", "None"));

    private String lastPopName = null;
    private long popFlashEndMs = 0L;

    public TotemAlert() {
        super("TotemAlert", "Plays sound and flashes when a nearby player pops a totem", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastPopName = null;
        popFlashEndMs = 0L;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.world == null || mc.player == null) return;
        if (!(event.getPacket() instanceof EntityStatusS2CPacket pkt)) return;
        if (pkt.getStatus() != 35) return;

        var entity = pkt.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity player)) return;
        if (entity == mc.player) return;
        if (mc.player.distanceTo(entity) > 64.0) return;

        lastPopName = player.getGameProfile().getName();
        popFlashEndMs = System.currentTimeMillis() + 1500;

        String soundMode = sound.get();
        if (!soundMode.equals("None") && mc.player != null) {
            var soundEvent = soundMode.equals("Explode")
                    ? SoundEvents.ENTITY_GENERIC_EXPLODE
                    : SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            mc.player.playSound(soundEvent, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (System.currentTimeMillis() >= popFlashEndMs || lastPopName == null) return;

        DrawContext ctx = event.getDrawContext();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        long remaining = popFlashEndMs - System.currentTimeMillis();
        float alpha = (float) remaining / 1500f;
        int a = (int) (alpha * 80);
        ctx.fill(0, 0, w, h, (a << 24) | 0xFFAA00);

        String msg = lastPopName + " popped a totem!";
        int tw = mc.textRenderer.getWidth(msg);
        ctx.drawTextWithShadow(mc.textRenderer, msg, (w - tw) / 2, h / 2 - 20, 0xFFFFAA00);
    }
}
