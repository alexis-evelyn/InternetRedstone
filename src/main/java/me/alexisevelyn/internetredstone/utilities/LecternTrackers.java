package me.alexisevelyn.internetredstone.utilities;

import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LecternTrackers {
    ConcurrentHashMap<Location, LecternTracker> trackers;

    public LecternTrackers() {
        trackers = new ConcurrentHashMap<>();
    }

    public LecternTracker registerTracker(Location location, UUID player) throws DuplicateObjectException {
        if (trackers.containsKey(location))
            throw new DuplicateObjectException(ChatColor.GOLD + "Tracker, "
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "("
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockX()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockY()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockZ()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ")"
                    + ChatColor.GOLD + ", already stored in database!!!");

        LecternTracker tracker = new LecternTracker(location, player);
        tracker.subscribe();

        trackers.put(location, tracker);

        return tracker;
    }

    public void unregisterTracker(Location location) throws MissingObjectException {
        if (!trackers.containsKey(location))
            throw new MissingObjectException(ChatColor.GOLD + "Tracker, "
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "("
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockX()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockY()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ", "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + location.getBlockZ()
                    + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ")"
                    + ChatColor.GOLD + ", missing from database!!!");

        LecternTracker tracker = trackers.get(location);
        tracker.cleanup();

        trackers.remove(location);
    }
}
