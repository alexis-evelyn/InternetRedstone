package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakLectern implements Listener {
    final LecternTrackers trackers;

    public BreakLectern(LecternTrackers trackers) {
        this.trackers = trackers;
    }

    // We want to be the last to know if the lectern is broken, that way we can ignore it if a claim plugin prevented the breakage
    // The way priorities work is that we get the last say at highest priority and therefor what we say happens.
    // However, we are not cancelling the event, we just want to make sure a claim plugin runs before we do,
    // so they take care of if it should be broken or not. That way, if it's cancelled, we can keep the lectern registered
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void breakLectern(BlockBreakEvent event) {
        BlockState snapshot = event.getBlock().getState();

        if (snapshot instanceof Lectern) {
            // Unregister Lectern With Plugin if Registered
            try {
                Location location = snapshot.getLocation();

                if (trackers.isRegistered(location))
                    trackers.unregisterTracker(location);
            } catch (MissingObjectException exception) {
                Logger.printException(exception);
            }
        }
    }
}
