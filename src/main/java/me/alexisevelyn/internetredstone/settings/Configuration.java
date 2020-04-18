package me.alexisevelyn.internetredstone.settings;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.hashids.Hashids;

import java.security.SecureRandom;
import java.util.UUID;

public class Configuration {
    final Main main;
    FileConfiguration config;
    Hashids hashids;
    final SecureRandom random = new SecureRandom();

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

        // Server Wide Settings
        config.addDefault("server.language", "en");
        config.addDefault("server.country", "");
        config.addDefault("server.variant", "");

        // Currently port and tls is not used
        config.addDefault("default.broker", "broker.hivemq.com");
        config.addDefault("default.port", 1883);
        config.addDefault("default.tls", false);
        config.addDefault("default.retain", true);

        // MQTT Client may not use username and password, so set to a blank value by default
        config.addDefault("default.username", "");
        config.addDefault("default.password", "");

        // Needs to be changed after set during first load
        config.addDefault("mysql.url", "jdbc:mysql://localhost:3306/internetredstone?useSSL=false");
        config.addDefault("mysql.username", "internetredstone");
        config.addDefault("mysql.password", "setMeToSomethingUnique");

        // Used to seed the unique short id hashing algorithm
        config.addDefault("lectern.id-hash", UUID.randomUUID().toString());

        // Number of Tries to generate a short and sweet id for each lectern if one is not specified
        config.addDefault("lectern.generate-short-id-tries", 3);

        // Max Integer To Try To Use For Short ID Generation
        config.addDefault("lectern.max-short-id", 10000);

        // Retrieve Hash ID Or Generate A New One On Failure
        try {
            hashids = new Hashids(config.getString("lectern.id-hash"));
        } catch (NullPointerException exception) {
            hashids = new Hashids(UUID.randomUUID().toString());
        }

        // Pick a random server name to start off with - Should be changed by server owner
        config.addDefault("server-name",
                hashids.encode(random.nextInt(config.getInt("lectern.max-short-id", 10000))));

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
//        main.reloadConfig();
//    }
// --Commented out by Inspection STOP (4/9/20, 7:35 PM)
}
