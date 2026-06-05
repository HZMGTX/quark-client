package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;

public class PermissionChecker extends Module {

    public PermissionChecker() {
        super("Perm Checker", "Check which staff commands are available", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        ChatUtil.info("§6[PermChecker] §fTesting permissions...");
        if (mc.getNetworkHandler() != null) {
            ChatUtil.info("§7Server brand: §f" + mc.getNetworkHandler().getBrand());
        }
        var abilities = mc.player.getAbilities();
        ChatUtil.info("§7Allow Flying: §f" + abilities.allowFlying);
        ChatUtil.info("§7Creative: §f" + abilities.creativeMode);
        ChatUtil.info("§7Invulnerable: §f" + abilities.invulnerable);
        ChatUtil.info("§7Instabuild: §f" + abilities.creativeMode);
        disable();
    }
}
