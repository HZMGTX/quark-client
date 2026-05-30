package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardPlayerActor;
import net.minecraft.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * TeamMode — tracks teammates so other combat modules can skip them.
 * Provides a static {@link #isTeammate(Entity)} helper.
 */
public class TeamMode extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to detect teammates",
            "Name", "Color", "Name", "Both"));

    private final BoolSetting friendsAreTeam = register(new BoolSetting(
            "Friends As Team", "Also treat friend-listed players as teammates", true));

    private static TeamMode instance;

    /** Players currently considered teammates (updated each tick). */
    private final List<String> teammates = new ArrayList<>();

    public TeamMode() {
        super("TeamMode", "Marks teammates so auras skip them", Category.COMBAT);
        instance = this;
    }

    @Override
    public void onDisable() {
        teammates.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        teammates.clear();
        Team localTeam = (Team) mc.player.getScoreboardTeam();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;

            String name = player.getGameProfile().getName();
            boolean isTeam = false;

            if (localTeam != null) {
                Team theirTeam = (Team) player.getScoreboardTeam();
                switch (mode.get()) {
                    case "Name" -> isTeam = localTeam.equals(theirTeam);
                    case "Color" -> {
                        if (theirTeam != null) {
                            isTeam = localTeam.getColor() == theirTeam.getColor();
                        }
                    }
                    case "Both" -> {
                        if (theirTeam != null) {
                            isTeam = localTeam.equals(theirTeam)
                                    || localTeam.getColor() == theirTeam.getColor();
                        }
                    }
                }
            }

            if (!isTeam && friendsAreTeam.isEnabled()) {
                if (cc.quark.Quark.getInstance() != null) {
                    isTeam = cc.quark.Quark.getInstance().getFriendManager().isFriend(name);
                }
            }

            if (isTeam) teammates.add(name);
        }
    }

    /**
     * Returns true if the given entity is a teammate. Safe to call from any module.
     */
    public static boolean isTeammate(Entity entity) {
        if (instance == null || !instance.isEnabled()) return false;
        if (!(entity instanceof PlayerEntity player)) return false;
        String name = player.getGameProfile().getName();
        return instance.teammates.contains(name);
    }
}
