package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class MemoryOptimizer extends Module {

    private final IntSetting  interval  = register(new IntSetting("Interval",  "Seconds between GC calls",                60, 30, 120));
    private final BoolSetting particles = register(new BoolSetting("Particles", "Reduce particle effects to MINIMAL level", true));

    private final TimerUtil timer = new TimerUtil();

    public MemoryOptimizer() {
        super("MemoryOptimizer", "Periodically runs GC and reduces particle effects to save memory", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
        applyParticles();
    }

    @Override
    public void onDisable() {
        if (particles.isEnabled() && mc.options != null)
            mc.options.getParticles().setValue(net.minecraft.client.option.ParticlesMode.ALL);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (!timer.hasReached((long) interval.get() * 1000L)) return;
        timer.reset();
        long before = Runtime.getRuntime().freeMemory();
        System.gc();
        long freed = Runtime.getRuntime().freeMemory() - before;
        ChatUtil.info("MemoryOptimizer: freed ~" + (freed / 1024 / 1024) + " MB");
        applyParticles();
    }

    private void applyParticles() {
        if (particles.isEnabled() && mc.options != null)
            mc.options.getParticles().setValue(net.minecraft.client.option.ParticlesMode.MINIMAL);
    }
}
