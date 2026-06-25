package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class DamageFlash extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Minimum damage to trigger flash", 4, 1, 20));

    private final DoubleSetting flashDuration = register(new DoubleSetting(
            "Flash Duration", "How many seconds the flash stays visible", 0.5, 0.1, 3.0));

    private float prevHealth = -1f;
    private long flashEndMs = 0L;

    public DamageFlash() {
        super("DamageFlash", "Flashes screen red when taking damage above threshold", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        prevHealth = -1f;
        flashEndMs = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float health = mc.player.getHealth();
        if (prevHealth >= 0f) {
            float delta = prevHealth - health;
            if (delta >= threshold.get()) {
                flashEndMs = System.currentTimeMillis() + (long) (flashDuration.get() * 1000.0);
            }
        }
        prevHealth = health;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (System.currentTimeMillis() >= flashEndMs) return;

        DrawContext ctx = event.getDrawContext();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        long remaining = flashEndMs - System.currentTimeMillis();
        long total = (long) (flashDuration.get() * 1000.0);
        float alpha = (float) remaining / total;
        int a = (int) (alpha * 120);

        ctx.fill(0, 0, w, h, (a << 24) | 0xFF0000);
    }
}
