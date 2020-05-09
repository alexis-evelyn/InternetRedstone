package me.alexisevelyn.internetredstone.utilities;

import lombok.SneakyThrows;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.utilities.data.Tracker;
import me.alexisevelyn.internetredstone.utilities.data.DisconnectReason;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IllegalFormatException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LecternHandlers {
    // Main class used to get synchronous execution
    final Main main;

    // Server Translation Language
    final Translator translator;

    // List of Tracker Objects
    final private ConcurrentHashMap<Location, LecternHandler> handlers;

    // ExecutorService to Asynchronously Load Handlers
    final private ExecutorService exec;

    public LecternHandlers(Main main) {
        this.main = main;
        translator = main.getServerTranslator();

        handlers = new ConcurrentHashMap<>();

        // Load Fixed Thread Pool Limited To Number of Processors - Including Cores
        exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void registerSavedHandlers() {
        MySQLClient mySQLClient = main.getMySQLClient();

        try {
            ResultSet lecternsInfo = mySQLClient.retrieveAllRegisteredLecternsIfExists();

            Logger.info(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_startup_registering_all"));

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

                Logger.finer(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                        + translator.getString("lectern_startup_register")
                        + Logger.getFormattedLocation(lectern));

            }

        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_startup_failed_register_all"));
            Logger.printException(exception);
        }
    }

    @SneakyThrows
    public void registerHandler(Location location, UUID player) {
        if (handlers.containsKey(location)) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_already_registered_warning"),
                        Logger.getFormattedLocation(location) + ChatColor.GOLD + ChatColor.BOLD);
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_already_registered_warning");
            }

            throw new DuplicateObjectException(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + warning);
        }

        // TODO: Make Loading Lecterns Asynchronous
//        Object handler = null;
//        exec.submit((Runnable) new LecternHandler(main, location, player), handler);

        LecternHandler handler = new LecternHandler(main, location, player);
        handlers.put(location, handler);
    }

    public void unregisterHandler(Location location) {
        unregisterHandler(location, new DisconnectReason(DisconnectReason.Reason.UNSPECIFIED));
    }

    @SneakyThrows
    public void unregisterHandler(Location location, DisconnectReason reason) {
        if (!handlers.containsKey(location)) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_missing_warning"),
                        Logger.getFormattedLocation(location) + ChatColor.GOLD + ChatColor.BOLD);
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_missing_warning");
            }

            throw new MissingObjectException(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + warning);
        }

        LecternHandler handler = handlers.get(location);
        handler.unregister(reason);

        handlers.remove(location);
    }

    public boolean isRegistered(Location location) {
        return handlers.containsKey(location);
    }

    @lombok.SneakyThrows
    public LecternHandler getHandler(Location location) {
        if (!isRegistered(location)) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_missing_warning"),
                        Logger.getFormattedLocation(location) + ChatColor.GOLD + ChatColor.BOLD);
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_missing_warning");
            }

            throw new MissingObjectException(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + warning);
        }


        return handlers.get(location);
    }

    public ConcurrentHashMap<Location, LecternHandler> getHandlers() {
        return handlers;
    }

    public void updatePlayerLocale(UUID player, String locale) {
        for (LecternHandler handler : handlers.values()) {
            if (handler.getPlayer().equals(player)) {
                handler.setPlayerLocale(locale);
            }
        }
    }

    public void cleanup(DisconnectReason disconnectReason) {
        for (Tracker tracker : handlers.values()) {
            // Tell tracker to perform cleanup duties (e.g. finish sending data/saving/etc...)
            tracker.cleanup(disconnectReason);

            // Mark tracker as eligible for garbage collection
            //noinspection UnusedAssignment
            tracker = null;
        }

        // Shutdown ExecutorService when it's finished!!!
        exec.shutdown();
    }
}
