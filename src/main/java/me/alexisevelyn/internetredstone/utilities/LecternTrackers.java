package me.alexisevelyn.internetredstone.utilities;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.utilities.abstracted.Tracker;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void registerSavedTrackers() {
        MySQLClient mySQLClient = main.getMySQLClient();

        try {
            ResultSet lecternsInfo = mySQLClient.retrieveAllRegisteredLecternsIfExists();

            Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD +
                    "Registering All Lecterns From Database (If Any)...");

            UUID player;
            World world;
            Location lectern = new Location(null, -1, -1, -1);

            while (lecternsInfo.next()) {
                player = UUID.fromString(lecternsInfo.getString("uuid"));

                lectern.set(lecternsInfo.getDouble("x"),
                        lecternsInfo.getDouble("y"),
                        lecternsInfo.getDouble("z"));

                world = Bukkit.getWorld(
                            UUID.fromString(
                                    lecternsInfo.getString("worldUID")
                            ));

                lectern.setWorld(world);

                try {
                    registerTracker(lectern, player);
                } catch (DuplicateObjectException exception) {
                    Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD +
                            "Duplicate Lectern Found in Database On Startup!!!");

                    Logger.printException(exception);
                }
            }

        } catch (SQLException exception) {
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD +
                    "Failed To Register All Lecterns From Database!!!");

            Logger.printException(exception);
        }
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

    public void cleanup() {
        for (Tracker tracker : trackers.values()) {
            // Tell tracker to perform cleanup duties (e.g. finish sending data/saving/etc...)
            tracker.cleanup();

            // Mark tracker as eligible for garbage collection
            //noinspection UnusedAssignment
            tracker = null;
        }
    }
}
