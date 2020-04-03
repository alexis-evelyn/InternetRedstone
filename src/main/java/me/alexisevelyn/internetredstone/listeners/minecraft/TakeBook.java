package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class TakeBook implements Listener {
    final LecternTrackers trackers;

    public TakeBook(LecternTrackers trackers) {
        this.trackers = trackers;
    }

    // We get called last, so a claim plugin can handle their stuff before we get the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
