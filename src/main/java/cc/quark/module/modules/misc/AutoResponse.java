package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

public class AutoResponse extends Module {
    private final StringSetting trigger  = register(new StringSetting("Trigger","Word that triggers auto-response","hello"));
    private final StringSetting response = register(new StringSetting("Response","Response message to send","Hey!"));
    private final BoolSetting   caseSens = register(new BoolSetting("Case Sensitive","Match case exactly",false));

    public AutoResponse() { super("AutoResponse","Auto-replies to chat messages containing a keyword",Category.MISC); }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = caseSens.isEnabled()?event.getMessage():event.getMessage().toLowerCase();
        String kw  = caseSens.isEnabled()?trigger.get():trigger.get().toLowerCase();
        if (msg.contains(kw) && mc.player!=null && mc.getNetworkHandler()!=null)
            mc.getNetworkHandler().sendChatMessage(response.get());
    }
}
