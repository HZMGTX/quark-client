package cc.quark.module.modules.player;

import cc.quark.module.Category;
import cc.quark.module.Module;

public class FakePlayer extends Module {

    public FakePlayer() {
        super("FakePlayer", "Creates a fake player entity at current position (cosmetic stub)", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("[Quark] FakePlayer activated (cosmetic stub)"), false);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("[Quark] FakePlayer deactivated"), false);
        }
    }
}
