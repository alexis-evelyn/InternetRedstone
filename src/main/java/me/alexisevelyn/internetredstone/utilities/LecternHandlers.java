package me.alexisevelyn.internetredstone.utilities;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.utilities.abstracted.Tracker;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LecternHandlers {
    // Main class used to get synchronous execution
    final Main main;

    // List of Tracker Objects
    final private ConcurrentHashMap<Location, LecternHandler> handlers;

    public LecternHandlers(Main main) {
        this.main = main;

        handlers = new ConcurrentHashMap<>();
    }

    public void registerSavedHandlers() {
        MySQLClient mySQLClient = main.getMySQLClient();

        try {
            ResultSet lecternsInfo = mySQLClient.retrieveAllRegisteredLecternsIfExists();

            Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD +
                    "Registering All Lecterns From Database (If Any)...");

            UUID player;
            World world;
            Location lectern;

            while (lecternsInfo.next()) {
                player = UUID.fromString(lecternsInfo.getString("uuid"));
                world = Bukkit.getWorld(
                            UUID.fromString(
                                    lecternsInfo.getString("worldUID")
                            ));

                lectern = new Location(world,
                        lecternsInfo.getDouble("x"),
                        lecternsInfo.getDouble("y"),
                        lecternsInfo.getDouble("z"));

                registerHandler(lectern, player);

                Logger.finer(ChatColor.GOLD + "" + ChatColor.BOLD +
                        "Registered Lectern At: " +
                        Logger.getFormattedLocation(lectern));

            }

        } catch (SQLException exception) {
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD +
                    "Failed To Register All Lecterns From Database!!!");

            Logger.printException(exception);
        }
    }

    public void registerHandler(Location location, UUID player) {
        if (handlers.containsKey(location))
            return;

        LecternHandler handler = new LecternHandler(main, location, player);
        handlers.put(location, handler);
    }

    public void unregisterHandler(Location location) throws MissingObjectException {
        if (!handlers.containsKey(location))
            throw new MissingObjectException(ChatColor.GOLD + "Tracker, "
                    + Logger.getFormattedLocation(location)
                    + ChatColor.GOLD + "" + ChatColor.BOLD
                    + ", missing from database!!!");

        LecternHandler handler = handlers.get(location);
        handler.unregister();

        handlers.remove(location);
    }

    public boolean isRegistered(Location location) {
        return handlers.containsKey(location);
    }

    @lombok.SneakyThrows
    public LecternHandler getHandler(Location location) {
        if (!isRegistered(location))
            throw new MissingObjectException(ChatColor.GOLD + "Tracker, "
                    + Logger.getFormattedLocation(location)
                    + ChatColor.GOLD + ", missing from database!!!");

        return handlers.get(location);
    }

    public ConcurrentHashMap<Location, LecternHandler> getHandlers() {
        return handlers;
    }

    public void cleanup() {
        for (Tracker tracker : handlers.values()) {
            // Tell tracker to perform cleanup duties (e.g. finish sending data/saving/etc...)
            tracker.cleanup();

            // Mark tracker as eligible for garbage collection
            //noinspection UnusedAssignment
            tracker = null;
        }
    }
}
