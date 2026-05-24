package com.ghostclient.friend;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Maintains the list of players that modules (e.g., KillAura) should skip.
 * The list is persisted to {@code .minecraft/ghostclient/friends.json}.
 */
public class FriendManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("GhostClient/Friends");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Set<String> friends = new HashSet<>();
    private final Path friendsFile;

    public FriendManager() {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        Path ghostDir = gameDir.toPath().resolve("ghostclient");
        this.friendsFile = ghostDir.resolve("friends.json");

        // Attempt to load on construction so friends are ready before modules start.
        try {
            load();
        } catch (Exception e) {
            LOGGER.warn("Could not load friends list: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * Add a player to the friends list (case-insensitive).
     *
     * @param username the player's username
     */
    public void addFriend(String username) {
        if (username == null || username.isBlank()) return;
        friends.add(username.toLowerCase());
        save();
    }

    /**
     * Remove a player from the friends list.
     *
     * @param username the player's username
     */
    public void removeFriend(String username) {
        if (username == null) return;
        friends.remove(username.toLowerCase());
        save();
    }

    /**
     * Returns true when the given username is on the friends list.
     *
     * @param username the player's username
     * @return true if friend
     */
    public boolean isFriend(String username) {
        if (username == null) return false;
        return friends.contains(username.toLowerCase());
    }

    /**
     * Returns an unmodifiable view of the friends list.
     */
    public Set<String> getFriends() {
        return friends;
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    /**
     * Save the current friends list to disk.
     */
    public void save() {
        try {
            Files.createDirectories(friendsFile.getParent());
            JsonArray array = new JsonArray();
            for (String name : friends) {
                array.add(name);
            }
            try (Writer writer = Files.newBufferedWriter(friendsFile, StandardCharsets.UTF_8)) {
                GSON.toJson(array, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save friends list: {}", e.getMessage());
        }
    }

    /**
     * Load the friends list from disk. Clears current list first.
     */
    public void load() {
        if (!Files.exists(friendsFile)) return;

        try (Reader reader = Files.newBufferedReader(friendsFile, StandardCharsets.UTF_8)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            friends.clear();
            for (JsonElement element : array) {
                friends.add(element.getAsString().toLowerCase());
            }
            LOGGER.info("Loaded {} friend(s).", friends.size());
        } catch (Exception e) {
            LOGGER.warn("Failed to load friends list: {}", e.getMessage());
        }
    }
}
