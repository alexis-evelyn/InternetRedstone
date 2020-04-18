package me.alexisevelyn.internetredstone.utilities.abstracted;

import lombok.Data;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.utilities.data.DisconnectReason;
import org.bukkit.Location;

import java.util.UUID;

@Data
public abstract class Tracker {
    // Used to track the location of the lectern. Includes the world object too!
    Location location;

    // Used to prevent duplicate signals from being sent
    Integer lastKnownPower = -1;

    // Used to track the player who owns the lectern. Will aid in allowing using that player's settings.
    UUID player;

    // Main class used to get synchronous execution (Don't Mark as Final Here)
    Main main;

    public boolean isLastKnownPower(Integer currentPower) {
        return currentPower.equals(getLastKnownPower());
    }

    // Expected of All Trackers
    public abstract void cleanup(DisconnectReason disconnectReason);
}
