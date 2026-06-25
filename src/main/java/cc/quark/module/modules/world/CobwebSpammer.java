package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CobwebSpammer extends Module {
    private final IntSetting radius = register(new IntSetting("Radius", "Spam radius", 2, 1, 5));
    private final BoolSetting onlyOnTarget = register(new BoolSetting("OnlyOnTarget", "Only spam on targeted entities", true));
    public CobwebSpammer() { super("CobwebSpammer", "Rapidly places cobwebs around enemies", Category.WORLD); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getMainHandStack().getItem() != Items.COBWEB) return;
    }
}
