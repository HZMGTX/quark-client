package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class MovementHUD extends Module {

    private final IntSetting  posX    = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY    = register(new IntSetting("Y", "HUD Y position", 50, 0, 3000));
    private final BoolSetting showSprint  = register(new BoolSetting("Sprint",  "Show sprint indicator",   true));
    private final BoolSetting showSneak   = register(new BoolSetting("Sneak",   "Show sneak indicator",    true));
    private final BoolSetting showSwim    = register(new BoolSetting("Swim",    "Show swim indicator",     true));
    private final BoolSetting showFly     = register(new BoolSetting("Fly",     "Show fly indicator",      true));
    private final BoolSetting showGround  = register(new BoolSetting("Ground",  "Show on-ground indicator",false));
    private final BoolSetting showClimb   = register(new BoolSetting("Climb",   "Show climbing indicator", true));
    private final BoolSetting compact     = register(new BoolSetting("Compact",  "Show states on one line", false));

    public MovementHUD() {
        super("MovementHUD", "Shows movement state (sprinting, sneaking, swimming, flying) on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();

        List<String> active = new ArrayList<>();
        List<String> inactive = new ArrayList<>();

        if (showSprint.isEnabled()) {
            if (mc.player.isSprinting()) active.add("§aSPRINT");
            else inactive.add("§7sprint");
        }
        if (showSneak.isEnabled()) {
            if (mc.player.isSneaking()) active.add("§eSNEAK");
            else inactive.add("§7sneak");
        }
        if (showSwim.isEnabled()) {
            if (mc.player.isSwimming() || mc.player.isTouchingWater()) active.add("§bSWIM");
            else inactive.add("§7swim");
        }
        if (showFly.isEnabled()) {
            if (mc.player.getAbilities().flying) active.add("§dFLY");
            else inactive.add("§7fly");
        }
        if (showClimb.isEnabled()) {
            if (mc.player.isClimbing()) active.add("§6CLIMB");
            else inactive.add("§7climb");
        }
        if (showGround.isEnabled()) {
            if (mc.player.isOnGround()) active.add("§fGROUND");
            else inactive.add("§7ground");
        }

        if (compact.isEnabled()) {
            // All on one line
            List<String> all = new ArrayList<>(active);
            all.addAll(inactive);
            if (!all.isEmpty()) {
                ctx.drawTextWithShadow(mc.textRenderer, String.join(" ", all), x, y, 0xFFFFFFFF);
            }
        } else {
            int lh = mc.textRenderer.fontHeight + 2;
            for (String s : active) {
                ctx.drawTextWithShadow(mc.textRenderer, s, x, y, 0xFFFFFFFF);
                y += lh;
            }
            for (String s : inactive) {
                ctx.drawTextWithShadow(mc.textRenderer, s, x, y, 0xFF555555);
                y += lh;
            }
        }
    }
}
