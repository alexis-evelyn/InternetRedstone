package me.alexisevelyn.internetredstone;

import me.alexisevelyn.internetredstone.listeners.minecraft.InteractWithLectern;
import me.alexisevelyn.internetredstone.listeners.minecraft.RedstoneUpdate;
import me.alexisevelyn.internetredstone.listeners.minecraft.TakeBook;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new RedstoneUpdate(), this);
        getServer().getPluginManager().registerEvents(new InteractWithLectern(), this);
        getServer().getPluginManager().registerEvents(new TakeBook(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
