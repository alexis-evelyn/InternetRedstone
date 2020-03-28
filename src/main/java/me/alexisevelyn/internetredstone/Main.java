package me.alexisevelyn.internetredstone;

import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.listeners.minecraft.InteractWithLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.RedstoneUpdate;
import me.alexisevelyn.internetredstone.listeners.minecraft.TakeBook;
import me.alexisevelyn.internetredstone.utilities.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.LecternTrackers;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    MySQLClient client;
    LecternTrackers trackers;

    @Override
    public void onEnable() {
        // Plugin startup logic
        trackers = new LecternTrackers();

        getServer().getPluginManager().registerEvents(new RedstoneUpdate(), this);
        getServer().getPluginManager().registerEvents(new InteractWithLectern(trackers), this);
        getServer().getPluginManager().registerEvents(new TakeBook(trackers), this);

        client = new MySQLClient();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
