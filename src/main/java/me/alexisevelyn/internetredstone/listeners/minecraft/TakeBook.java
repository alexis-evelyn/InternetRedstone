package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class TakeBook implements Listener {
    LecternTrackers trackers;

    public TakeBook(LecternTrackers trackers) {
        this.trackers = trackers;
    }

    @EventHandler
    public void takeBook(PlayerTakeLecternBookEvent event) {
        // Unregister Lectern With Plugin if Registered
        try {
            Location location = event.getLectern().getLocation();

            if (trackers.isRegistered(location))
                trackers.unregisterTracker(location);
        } catch (MissingObjectException exception) {
            Logger.printException(exception);
        }
    }
}
