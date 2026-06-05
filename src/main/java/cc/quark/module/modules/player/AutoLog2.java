package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.network.ServerInfo;

public class AutoLog2 extends Module {

    private final DoubleSetting hp = register(new DoubleSetting(
            "HP", "Health threshold to auto-disconnect", 3.0, 1.0, 10.0));

    private final IntSetting graceMs = register(new IntSetting(
            "Grace (ms)", "Milliseconds to wait before disconnecting after HP drops", 500, 0, 3000));

    private long belowHpSince = -1;

    public AutoLog2() {
        super("AutoLog2", "Auto-logout with grace period", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        belowHpSince = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        float health = mc.player.getHealth();
        if (health <= hp.get()) {
            if (belowHpSince == -1) {
                belowHpSince = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - belowHpSince >= graceMs.get()) {
                mc.getNetworkHandler().getConnection().disconnect(
                        net.minecraft.text.Text.literal("AutoLog2: Health critical"));
                belowHpSince = -1;
                this.toggle();
            }
        } else {
            belowHpSince = -1;
        }
    }
}
