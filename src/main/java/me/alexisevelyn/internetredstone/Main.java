package me.alexisevelyn.internetredstone;

import io.reactivex.plugins.RxJavaPlugins;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.listeners.minecraft.BreakLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.InteractWithLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.RedstoneUpdate;
import me.alexisevelyn.internetredstone.listeners.minecraft.TakeBook;
import me.alexisevelyn.internetredstone.listeners.minecraft.commands.Lecterns;
import me.alexisevelyn.internetredstone.settings.Configuration;
import me.alexisevelyn.internetredstone.utilities.LecternHandlers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.util.Objects;

public class Main extends JavaPlugin {
    private LecternHandlers handlers;

    private Configuration config;
    private MySQLClient client;

    @Override
    public void onEnable() {
        // Setup and Load Config
        config = new Configuration(this);

        // Register MySQL Client
        client = new MySQLClient(this);

        // Register bStats
        Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                + "bStats Enabled: "
                + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                + registerStats());

        // Register error handler for asynchronous functions
        RxJavaPlugins.setErrorHandler(Logger::rxHandler);

        // Register lectern trackers class
        handlers = new LecternHandlers(this);

        // Register Saved Trackers From Database
        handlers.registerSavedHandlers();

        // Register Bukkit Event Listeners
        getServer().getPluginManager().registerEvents(new RedstoneUpdate(handlers), this);
        getServer().getPluginManager().registerEvents(new InteractWithLectern(handlers), this);
        getServer().getPluginManager().registerEvents(new TakeBook(handlers), this);
        getServer().getPluginManager().registerEvents(new BreakLectern(handlers), this);

        // Register Bukkit Commands
        try {
            // TODO: Add support for player friendly command (as in not admin)
            //  Name it internetredstone or swap it and lecterns?

            PluginCommand lecterns = getCommand("lecterns");
            Objects.requireNonNull(lecterns).setExecutor(new Lecterns(handlers));

            PluginCommand internetredstone = getCommand("internetredstone");
            Objects.requireNonNull(internetredstone).setExecutor(new Lecterns(handlers));
        } catch (NullPointerException exception) {
            Logger.printException(exception);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Close MQTT Connections Properly (So, players can get notified on server shutdown/etc...)
        handlers.cleanup();
        handlers = null;

        // Close MySQL Connection
        client.disconnect();
        client = null;
    }

    protected Boolean registerStats() {
        final int pluginId = 7001;

        // bStats Tracker
        final Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("number_of_registered_lecterns", () -> {
            // Retrieves Number of Registered Lecterns - Nothing Else, Just The Number
            ResultSet lecternCount = client.getNumberOfRegisteredLecterns();

            if (lecternCount.next()) {
                return lecternCount.getString(1);
            }

            return "Failed To Retrieve Number of Registered Lecterns";
        }));

        metrics.addCustomChart(new Metrics.SimplePie("number_of_registered_players", () -> {
            // Retrieves Number of Registered Players - Nothing Else, Just The Number
            ResultSet playerCount = client.getNumberOfRegisteredPlayers();

            if (playerCount.next()) {
                return playerCount.getString(1);
            }

            return "Failed To Retrieve Number of Registered Players";
        }));

        return metrics.isEnabled();
    }

    public MySQLClient getMySQLClient() {
        return client;
    }

    public Configuration getConfiguration() {
        return config;
    }
}
