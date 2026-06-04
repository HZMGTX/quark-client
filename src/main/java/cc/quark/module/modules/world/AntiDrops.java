package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;

public class AntiDrops extends Module {

    private final BoolSetting arrow       = register(new BoolSetting("Arrow",       "Ignore arrow pickups",       true));
    private final BoolSetting experience  = register(new BoolSetting("Experience",  "Ignore experience orbs",     true));
    private final BoolSetting rottenFlesh = register(new BoolSetting("Rotten Flesh","Ignore rotten flesh pickups",true));
    private final BoolSetting bone        = register(new BoolSetting("Bone",        "Ignore bone pickups",        true));
    private final BoolSetting gravel      = register(new BoolSetting("Gravel",      "Ignore gravel pickups",      false));
    private final BoolSetting cobblestone = register(new BoolSetting("Cobblestone", "Ignore cobblestone pickups", false));

    public AntiDrops() {
        super("AntiDrops", "Cancels pickup animations for junk/unwanted items", Category.WORLD);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.world == null) return;
        if (!(event.getPacket() instanceof ItemPickupAnimationS2CPacket pkt)) return;

        int entityId = pkt.id();
        Entity entity = mc.world.getEntityById(entityId);
        if (!(entity instanceof ItemEntity itemEntity)) return;

        var item = itemEntity.getStack().getItem();

        if (arrow.isEnabled()       && item == Items.ARROW)        { event.cancel(); return; }
        if (rottenFlesh.isEnabled() && item == Items.ROTTEN_FLESH)  { event.cancel(); return; }
        if (bone.isEnabled()        && item == Items.BONE)           { event.cancel(); return; }
        if (gravel.isEnabled()      && item == Items.GRAVEL)         { event.cancel(); return; }
        if (cobblestone.isEnabled() && item == Items.COBBLESTONE)    { event.cancel(); return; }
    }
}
