package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleSearch extends Module {

    private final StringSetting query = register(new StringSetting(
            "Query", "Search term to highlight matching modules in the array list", ""));

    public ModuleSearch() {
        super("ModuleSearch", "Highlights modules matching the search query in the HUD", Category.MISC);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        String q = query.get().toLowerCase().trim();
        if (q.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        List<Module> matches = Quark.getInstance().getModuleManager().getModules()
                .stream()
                .filter(m -> m.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§cNo match: " + query.get(), 4, 4, 0xFFFF4444);
            return;
        }

        int y = 4;
        ctx.drawTextWithShadow(mc.textRenderer, "§eSearch: §f" + query.get(), 4, y, 0xFFFFFFFF);
        y += mc.textRenderer.fontHeight + 2;
        for (Module m : matches) {
            String text = (m.isEnabled() ? "§a" : "§7") + m.getName();
            ctx.drawTextWithShadow(mc.textRenderer, text, 4, y, 0xFFFFFFFF);
            y += mc.textRenderer.fontHeight + 1;
        }
    }
}
