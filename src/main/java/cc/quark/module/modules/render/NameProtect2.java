package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

public class NameProtect2 extends Module {

    private final StringSetting fakeName = register(new StringSetting(
            "Fake Name", "Name to display instead of your real name", "Player"));

    private final BoolSetting skin = register(new BoolSetting(
            "Skin", "Apply name protection to skin identifier", false));

    private String originalName = null;

    public NameProtect2() {
        super("NameProtect2", "Replaces your name in chat/tablist", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            originalName = mc.player.getGameProfile().getName();
        }
    }

    @Override
    public void onDisable() {
        originalName = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Name replacement is done via mixin in NameTag/ChatRenderer.
        // This module provides the configured fake name via a static getter.
    }

    public String getFakeName() {
        return fakeName.get();
    }

    public boolean isSkinProtected() {
        return skin.isEnabled();
    }
}
