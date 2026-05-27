package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import cc.quark.mixin.IMinecraftClient;

public class FastPlace extends Module {

    public static FastPlace INSTANCE;

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between placements (0 = no delay)", 0, 0, 5));

    private final BoolSetting onHold = register(new BoolSetting(
            "On Hold", "Only reduce delay while right-click is held", true));

    public FastPlace() {
        super("FastPlace", "Reduces right-click placement delay to zero", Category.PLAYER);
        INSTANCE = this;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.interactionManager == null) return;

        if (onHold.isEnabled() && !mc.options.useKey.isPressed()) return;

        int current = ((IMinecraftClient) mc).getItemUseCooldown();
        if (current > delay.get()) {
            ((IMinecraftClient) mc).setItemUseCooldown(delay.get());
        }
    }

    public int getDelay() {
        return delay.get();
    }
}
