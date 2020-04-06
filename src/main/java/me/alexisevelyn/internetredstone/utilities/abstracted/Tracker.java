package me.alexisevelyn.internetredstone.utilities.abstracted;

import org.bukkit.Location;

import java.util.UUID;

public abstract class Tracker {
    // LecternTracker was getting way too big and complicated, so I split it up
    // Not only can an abstract class help clean up code, but it can also be used to provide implementation specific methods
    // that aren't implemented in the abstract class.

    // Used to track the location of the lectern. Includes the world object too!
    Location location;

    // Used to prevent duplicate signals from being sent
    Integer lastKnownSignal = -1;

    // Used to track the player who owns the lectern. Will aid in allowing using that player's settings.
    UUID player;

    public boolean isLastKnownPower(Integer currentPower) {
        return currentPower.equals(getLastKnownPower());
    }

    public Integer getLastKnownPower() {
        return lastKnownSignal;
    }

    public void setLastKnownPower(Integer lastKnownSignal) {
        this.lastKnownSignal = lastKnownSignal;
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // Expected of All Trackers
    public abstract void cleanup();
}
