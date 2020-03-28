package me.alexisevelyn.internetredstone.listeners.minecraft;

import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
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
        // Do Nothing For Now!!!

        // This will be used to unregister a lectern with the plugin

        try {
            // TODO: Check if tracker is a special tracker

            trackers.unregisterTracker(event.getLectern().getLocation());
        } catch (MissingObjectException exception) {
            Logger.printException(exception);
        }
    }
}
