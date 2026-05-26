package cc.quark.waypoint;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class WaypointManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Quark/Waypoints");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Waypoint(String name, double x, double y, double z, int color) {}

    private static final int[] COLORS = {
        0xFF5555FF, 0xFF55FF55, 0xFFFF5555,
        0xFFFFFF55, 0xFFFF55FF, 0xFF55FFFF
    };

    private final List<Waypoint> waypoints = new ArrayList<>();
    private final Path waypointsFile;

    public WaypointManager() {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        this.waypointsFile = gameDir.toPath().resolve("quark").resolve("waypoints.json");
    }

    public void add(String name, double x, double y, double z) {
        int color = COLORS[waypoints.size() % COLORS.length];
        waypoints.removeIf(w -> w.name().equalsIgnoreCase(name));
        waypoints.add(new Waypoint(name, x, y, z, color));
        save();
    }

    public void remove(String name) {
        waypoints.removeIf(w -> w.name().equalsIgnoreCase(name));
        save();
    }

    public void clear() {
        waypoints.clear();
        save();
    }

    public List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public void save() {
        try {
            Files.createDirectories(waypointsFile.getParent());
            JsonArray array = new JsonArray();
            for (Waypoint w : waypoints) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", w.name());
                obj.addProperty("x", w.x());
                obj.addProperty("y", w.y());
                obj.addProperty("z", w.z());
                obj.addProperty("color", w.color());
                array.add(obj);
            }
            try (Writer writer = Files.newBufferedWriter(waypointsFile, StandardCharsets.UTF_8)) {
                GSON.toJson(array, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save waypoints: {}", e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(waypointsFile)) return;
        try (Reader reader = Files.newBufferedReader(waypointsFile, StandardCharsets.UTF_8)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            waypoints.clear();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                String name  = obj.get("name").getAsString();
                double x     = obj.get("x").getAsDouble();
                double y     = obj.get("y").getAsDouble();
                double z     = obj.get("z").getAsDouble();
                int color    = obj.get("color").getAsInt();
                waypoints.add(new Waypoint(name, x, y, z, color));
            }
            LOGGER.info("Loaded {} waypoint(s).", waypoints.size());
        } catch (Exception e) {
            LOGGER.warn("Failed to load waypoints: {}", e.getMessage());
        }
    }
}
