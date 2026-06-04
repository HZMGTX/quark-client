package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class Pathfinder extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Maximum pathfinding range in blocks", 50, 5, 100));

    private List<BlockPos> currentPath = new ArrayList<>();
    private int pathIndex = 0;
    private BlockPos targetPos = null;

    public Pathfinder() {
        super("Pathfinder", "Finds path to target location automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        currentPath.clear();
        pathIndex = 0;
        if (mc.player != null) {
            // Default target: 20 blocks north of player
            targetPos = mc.player.getBlockPos().north(20);
            List<BlockPos> path = findPath(mc.player.getBlockPos(), targetPos);
            if (path != null) {
                currentPath = path;
                pathIndex = 0;
                ChatUtil.info("Pathfinder: found path of " + path.size() + " steps.");
            } else {
                ChatUtil.warn("Pathfinder: no path found.");
            }
        }
    }

    @Override
    public void onDisable() {
        currentPath.clear();
        pathIndex = 0;
        if (mc.player != null) {
            mc.player.input.movementForward = 0;
            mc.player.input.movementSideways = 0;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) return;

        BlockPos next = currentPath.get(pathIndex);
        double dx = next.getX() + 0.5 - mc.player.getX();
        double dz = next.getZ() + 0.5 - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.6) {
            pathIndex++;
            return;
        }

        // Face toward next node
        float yaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
        mc.player.setYaw(yaw);
        mc.player.input.movementForward = 1.0f;

        // Jump over block if needed
        if (next.getY() > mc.player.getBlockPos().getY() && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    // ---- A* pathfinding stub ----

    private List<BlockPos> findPath(BlockPos start, BlockPos goal) {
        if (start.getManhattanDistance(goal) > range.get() * 2) return null;

        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Integer> gScore = new HashMap<>();
        PriorityQueue<BlockPos> open = new PriorityQueue<>(
                Comparator.comparingInt(p -> gScore.getOrDefault(p, Integer.MAX_VALUE) + heuristic(p, goal)));

        gScore.put(start, 0);
        open.add(start);

        int iterations = 0;
        while (!open.isEmpty() && iterations++ < 2000) {
            BlockPos current = open.poll();
            if (current.equals(goal) || current.getManhattanDistance(goal) <= 1) {
                return reconstructPath(cameFrom, current);
            }

            for (BlockPos neighbor : getNeighbors(current)) {
                int tentative = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                if (tentative < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentative);
                    open.add(neighbor);
                }
            }
        }
        return null;
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        if (mc.world == null) return neighbors;
        int[][] offsets = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{1,1,0},{-1,1,0},{0,1,1},{0,1,-1}};
        for (int[] o : offsets) {
            BlockPos n = pos.add(o[0], o[1], o[2]);
            if (isWalkable(n)) neighbors.add(n);
        }
        return neighbors;
    }

    private boolean isWalkable(BlockPos pos) {
        if (mc.world == null) return false;
        BlockState feet = mc.world.getBlockState(pos);
        BlockState head = mc.world.getBlockState(pos.up());
        BlockState floor = mc.world.getBlockState(pos.down());
        return feet.isAir() && head.isAir() && !floor.isAir();
    }

    private int heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos current) {
        LinkedList<BlockPos> path = new LinkedList<>();
        while (cameFrom.containsKey(current)) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }
        return path;
    }
}
