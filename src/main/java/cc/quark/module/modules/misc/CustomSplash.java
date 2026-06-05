package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;

public class CustomSplash extends Module {

    private final StringSetting text = register(new StringSetting(
            "Text", "Splash text to display on the title screen", "Quark.cc | The best client!"));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Splash text color (ARGB)", 0xFFFFFF00));

    private final IntSetting posX = register(new IntSetting(
            "X", "Horizontal position (negative = right-aligned from center)", 230, 0, 1000));

    private final IntSetting posY = register(new IntSetting(
            "Y", "Vertical position in pixels", 120, 0, 500));

    private final BoolSetting onlyTitle = register(new BoolSetting(
            "Only Title Screen", "Only show on title screen (not in-game)", true));

    public CustomSplash() {
        super("CustomSplash", "Shows custom splash text on the Minecraft title screen", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc == null) return;

        boolean isTitle = mc.currentScreen instanceof TitleScreen;
        if (onlyTitle.isEnabled() && !isTitle) return;
        if (!isTitle && mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        String splash = text.get();
        if (splash == null || splash.isEmpty()) return;

        ctx.drawTextWithShadow(mc.textRenderer, splash, posX.get(), posY.get(), color.get());
    }

    @Override
    public String getSuffix() {
        String t = text.get();
        return t.length() > 20 ? t.substring(0, 20) + "..." : t;
    }
}
