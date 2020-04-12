package me.alexisevelyn.internetredstone;

import io.reactivex.plugins.RxJavaPlugins;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.listeners.minecraft.BreakLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.InteractWithLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.RedstoneUpdate;
import me.alexisevelyn.internetredstone.listeners.minecraft.TakeBook;
import me.alexisevelyn.internetredstone.listeners.minecraft.commands.Commands;
import me.alexisevelyn.internetredstone.settings.Configuration;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.util.Objects;

public class Main extends JavaPlugin {
    LecternTrackers trackers;
    Metrics metrics;
    final Integer pluginId = 7001;

    Configuration config;
    MySQLClient client;

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
        trackers = new LecternTrackers(this);

        // Register Saved Trackers From Database
        trackers.registerSavedTrackers();

        // Register Bukkit Event Listeners
        getServer().getPluginManager().registerEvents(new RedstoneUpdate(trackers), this);
        getServer().getPluginManager().registerEvents(new InteractWithLectern(trackers), this);
        getServer().getPluginManager().registerEvents(new TakeBook(trackers), this);
        getServer().getPluginManager().registerEvents(new BreakLectern(trackers), this);

        // Register Bukkit Commands
        try {
            // TODO: Add support for player friendly command (as in not admin)
            //  Name it internetredstone or swap it and lecterns?

            PluginCommand lecterns = getCommand("lecterns");
            Objects.requireNonNull(lecterns).setExecutor(new Commands(trackers));

            PluginCommand internetredstone = getCommand("internetredstone");
            Objects.requireNonNull(internetredstone).setExecutor(new Commands(trackers));
        } catch (NullPointerException exception) {
            Logger.printException(exception);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Close MQTT Connections Properly (So, players can get notified on server shutdown/etc...)
        trackers.cleanup();
        trackers = null;

        // Close MySQL Connection
        client.disconnect();
        client = null;
    }

    protected Boolean registerStats() {
        // bStats Tracker
        metrics = new Metrics(this, pluginId);

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
