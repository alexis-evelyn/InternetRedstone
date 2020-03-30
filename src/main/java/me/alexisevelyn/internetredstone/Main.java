package me.alexisevelyn.internetredstone;

import io.reactivex.plugins.RxJavaPlugins;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.listeners.minecraft.InteractWithLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.RedstoneUpdate;
import me.alexisevelyn.internetredstone.listeners.minecraft.TakeBook;
import me.alexisevelyn.internetredstone.listeners.minecraft.commands.Commands;
import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    MySQLClient client;
    LecternTrackers trackers;

    @Override
    public void onEnable() {
        // Plugin startup logic
        RxJavaPlugins.setErrorHandler(Logger::rxHandler);

        trackers = new LecternTrackers(this);

        getServer().getPluginManager().registerEvents(new RedstoneUpdate(trackers), this);
        getServer().getPluginManager().registerEvents(new InteractWithLectern(trackers), this);
        getServer().getPluginManager().registerEvents(new TakeBook(trackers), this);

        try {
            PluginCommand lecterns = getCommand("lecterns");
            Objects.requireNonNull(lecterns).setExecutor(new Commands(trackers));
        } catch (NullPointerException exception) {
            Logger.printException(exception);
        }

        client = new MySQLClient();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
