package me.alexisevelyn.internetredstone.settings;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class Configuration {
    final Main main;
    FileConfiguration config;

    public Configuration(Main main) {
        this.main = main;

        setupConfig();
    }

    private void setupConfig() {
        // Start setting up dynamic config data
        config = main.getConfig();

        // Debug is for debugging plugin, version is to upgrade config later
        // Logger Print Color - Used to turn off color logging if your server doesn't support it
        config.addDefault("debug", false);
        config.addDefault("logger.print.color", true);
        config.addDefault("version", "0.0.1");

        // Currently port and tls is not used
        config.addDefault("default.broker", "broker.hivemq.com");
        config.addDefault("default.port", 1883);
        config.addDefault("default.tls", false);

        // MQTT Client may not use username and password, so set to null by default
        config.addDefault("default.username", null);
        config.addDefault("default.password", null);

        // Needs to be changed after set during first load
        config.addDefault("mysql.url", "jdbc:mysql://localhost:3306/internetredstone?useSSL=false");
        config.addDefault("mysql.username", "internetredstone");
        config.addDefault("mysql.password", "setMeToSomethingUnique");

        // Pick a random server name to start off with - Should be changed by server owner
        config.addDefault("server-name", UUID.randomUUID().toString());

        // Copy Default Values From Shipped Config Into Ram
        config.options().copyDefaults(true);
        main.saveConfig();

        // Determine whether or not to output debug information
        Logger.setDebugMode(config.getBoolean("debug", false));
        Logger.setColorMode(config.getBoolean("logger.print.color", true));
    }

    public FileConfiguration getConfig() {
        return config;
    }

// --Commented out by Inspection START (4/9/20, 7:35 PM):
//    public void reloadConfig() {
//        // TODO: Test Me!!!
//        // TODO: Rewrite Plugin To Reload on Demand And Gracefully Handle Missing Data (e.g without disabling ourselves)
//        main.reloadConfig();
//    }
// --Commented out by Inspection STOP (4/9/20, 7:35 PM)
}
