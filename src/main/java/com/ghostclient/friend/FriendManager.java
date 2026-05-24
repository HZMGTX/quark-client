package com.ghostclient.friend;

import java.util.HashSet;
import java.util.Set;

/**
 * Maintains the list of players that modules (like KillAura) should not target.
 */
public class FriendManager {

    private final Set<String> friends = new HashSet<>();

    public void addFriend(String username) {
        friends.add(username.toLowerCase());
    }

    public void removeFriend(String username) {
        friends.remove(username.toLowerCase());
    }

    public boolean isFriend(String username) {
        return friends.contains(username.toLowerCase());
    }

    public Set<String> getFriends() {
        return friends;
    }
}
