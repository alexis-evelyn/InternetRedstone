package me.alexisevelyn.internetredstone.utilities;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LecternTrackers {
    // Main class used to get synchronous execution
    final Main main;

    // List of Tracker Objects
    final ConcurrentHashMap<Location, LecternTracker> trackers;

    // Identifier to Look For In Order To Help Track Lecterns
    final static String identifier = "[Internet Redstone]";

    public LecternTrackers(Main main) {
        this.main = main;

        trackers = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("UnusedReturnValue")
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

        LecternTracker tracker = new LecternTracker(main, location, player);
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
        tracker.unregister();

        trackers.remove(location);
    }

    public boolean isRegistered(Location location) {
        return trackers.containsKey(location);
    }

    @lombok.SneakyThrows
    public LecternTracker getTracker(Location location) {
        if (!isRegistered(location))
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

        return trackers.get(location);
    }

    public static String getIdentifier() {
        return identifier;
    }

    public ConcurrentHashMap<Location, LecternTracker> getTrackers() {
        return trackers;
    }
}
