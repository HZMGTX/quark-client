package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;

public class TotemInfo extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 2, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 80, 0, 1080));

    private boolean hasTotem;
    private int totemCount;

    public TotemInfo() {
        super("Totem Info", "Display totem count and protection status", Category.COMBAT, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        hasTotem = mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) ||
                   mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
        totemCount = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) totemCount++;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        String status = hasTotem ? "§a✓ TOTEM" : "§c✗ NO TOTEM";
        ctx.drawText(mc.textRenderer, status + " §7(§f" + totemCount + "§7)", x.get(), y.get(), 0xFFFFFF, true);
    }
}
