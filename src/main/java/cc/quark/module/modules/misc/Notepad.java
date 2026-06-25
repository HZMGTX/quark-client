package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import net.minecraft.client.gui.DrawContext;

public class Notepad extends Module {

    private final StringSetting note1 = register(new StringSetting(
            "Note 1", "First notepad line", ""));

    private final StringSetting note2 = register(new StringSetting(
            "Note 2", "Second notepad line", ""));

    private final StringSetting note3 = register(new StringSetting(
            "Note 3", "Third notepad line", ""));

    public Notepad() {
        super("Notepad", "Simple in-game notepad", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Position at center-right
        int baseX = sw / 2 + 10;
        int baseY = sh / 2 - 20;

        int lh = mc.textRenderer.fontHeight + 2;

        // Background
        int maxWidth = 0;
        if (!note1.get().isEmpty()) maxWidth = Math.max(maxWidth, mc.textRenderer.getWidth(note1.get()));
        if (!note2.get().isEmpty()) maxWidth = Math.max(maxWidth, mc.textRenderer.getWidth(note2.get()));
        if (!note3.get().isEmpty()) maxWidth = Math.max(maxWidth, mc.textRenderer.getWidth(note3.get()));

        if (maxWidth == 0) return;

        ctx.fill(baseX - 2, baseY - 2, baseX + maxWidth + 4, baseY + lh * 3 + 2, 0xAA000000);

        int y = baseY;
        if (!note1.get().isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§e" + note1.get(), baseX, y, 0xFFFFFFFF);
            y += lh;
        }
        if (!note2.get().isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§e" + note2.get(), baseX, y, 0xFFFFFFFF);
            y += lh;
        }
        if (!note3.get().isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§e" + note3.get(), baseX, y, 0xFFFFFFFF);
        }
    }
}
