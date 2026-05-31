package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class GhostHit extends Module {

    private final BoolSetting silentSwing = register(new BoolSetting("SilentSwing", "Suppress visual arm swing animation", true));

    public GhostHit() {
        super("GhostHit", "Sends attack packets without triggering the visual arm swing", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.world == null) return;
        Entity target = event.getTarget();
        if (target == null) return;

        event.cancel();

        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));

        if (!silentSwing.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
