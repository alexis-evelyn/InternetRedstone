package me.alexisevelyn.internetredstone;

import io.reactivex.plugins.RxJavaPlugins;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.listeners.minecraft.BreakLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.InteractWithLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.RedstoneUpdate;
import me.alexisevelyn.internetredstone.listeners.minecraft.TakeBook;
import me.alexisevelyn.internetredstone.listeners.minecraft.commands.Commands;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.Callable;

public class Main extends JavaPlugin {
    LecternTrackers trackers;
    Metrics metrics;
    Integer pluginId = 7001;

    MySQLClient client;

    @Override
    public void onEnable() {
        // Determine whether or not to output debug information
        Logger.setDebugMode(true);

        // Register MySQL Client
        client = new MySQLClient();

        // Register bStats
        Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                + "bStats Enabled: "
                + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                + registerStats());

        // Register error handler for asynchronous functions
        RxJavaPlugins.setErrorHandler(Logger::rxHandler);

        // Register lectern trackers class
        trackers = new LecternTrackers(this);

        // Register Bukkit Event Listeners
        getServer().getPluginManager().registerEvents(new RedstoneUpdate(trackers), this);
        getServer().getPluginManager().registerEvents(new InteractWithLectern(trackers), this);
        getServer().getPluginManager().registerEvents(new TakeBook(trackers), this);
        getServer().getPluginManager().registerEvents(new BreakLectern(trackers), this);

        // Register Bukkit Commands
        try {
            PluginCommand lecterns = getCommand("lecterns");
            Objects.requireNonNull(lecterns).setExecutor(new Commands(trackers));
        } catch (NullPointerException exception) {
            Logger.printException(exception);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    protected Boolean registerStats() {
        // bStats Tracker
        metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("number_of_registered_lecterns", new Callable<String>() {
            @Override
            public String call() throws Exception {
                // Retrieves Number of Registered Lecterns - Nothing Else, Just The Number
                return String.valueOf(trackers.getTrackers().size());
            }
        }));

        return metrics.isEnabled();
    }

    public MySQLClient getMySQLClient() {
        return client;
    }
}
