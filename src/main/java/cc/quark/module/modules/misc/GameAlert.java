package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class GameAlert extends Module {
    private final BoolSetting   lowHP   = register(new BoolSetting  ("Low HP","Alert on low health",true));
    private final DoubleSetting hpThr   = register(new DoubleSetting("HP Threshold","Alert below this HP",6.0,1.0,20.0));
    private final BoolSetting   hunger  = register(new BoolSetting  ("Hunger","Alert on low hunger",true));
    private boolean wasLowHP = false;

    public GameAlert() { super("GameAlert","Flashes screen on critical game events",Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null) return;
        boolean nowLow = mc.player.getHealth() < hpThr.get();
        if (lowHP.isEnabled() && nowLow && !wasLowHP)
            mc.player.sendMessage(net.minecraft.text.Text.literal("§c⚠ LOW HEALTH: " + (int)mc.player.getHealth()), true);
        wasLowHP = nowLow;
        if (hunger.isEnabled() && mc.player.getHungerManager().getFoodLevel()<6)
            mc.player.sendMessage(net.minecraft.text.Text.literal("§e⚠ LOW HUNGER"), true);
    }
}
