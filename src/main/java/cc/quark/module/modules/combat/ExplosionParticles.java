package cc.quark.module.modules.combat;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

public class ExplosionParticles extends Module {
    private final BoolSetting removeSmoke = register(new BoolSetting("No Smoke", "Remove explosion smoke", true));
    private final BoolSetting removeBlast = register(new BoolSetting("No Blast", "Remove blast particles", true));

    public ExplosionParticles() {
        super("No Explosions", "Reduces explosion visual effects for cleaner PvP", Category.COMBAT, 0);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("[NoExplosions] Visual explosion reduction active. Requires particle mixin to take effect.");
    }
}
