package me.alexisevelyn.internetredstone.utilities.data;

import lombok.Data;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.Translator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Data
public class PlayerSettings {
    final UUID UUID;
    final Main main;
    final MySQLClient mySQLClient;

    String locale = "en";
    Translator translator;

    MQTTSettings mqttSettings;

    public PlayerSettings(UUID uuid, Main main) {
        this.UUID = uuid;
        this.main = main;

        this.mySQLClient = main.getMySQLClient();

        loadPlayerSettings();
    }

    private void loadPlayerSettings() {
        // This string will be read from Player's Config Or If None Provided, Server's Config
        FileConfiguration config = main.getConfiguration().getConfig();

        // Connection Settings
        mqttSettings.setBroker(config.getString("default.broker"));
        mqttSettings.setPort(config.getInt("default.port", 1883));
        mqttSettings.setTls(config.getBoolean("default.tls", false));

        // Authentication
        mqttSettings.setSimpleAuth(config.getString("default.username", null),
                config.getString("default.password", null));

        // Determine if Should Send Retained Messages Or Not
        mqttSettings.setRetainMessage(config.getBoolean("default.retain", true));

        ResultSet playerData;
        try {
            playerData = mySQLClient.retrievePlayerDataIfExists(UUID);
            if (playerData != null && playerData.next()) {
                if (StringUtils.isNotBlank(playerData.getString("broker")))
                    mqttSettings.setBroker(playerData.getString("broker"));

                // You have to read the result first before checking if it's null
                int tempInt = playerData.getInt("port");
                if (!playerData.wasNull())
                    mqttSettings.setPort(tempInt);

                if (StringUtils.isNotBlank(playerData.getString("username")))
                    mqttSettings.setSimpleAuth(playerData.getString("username"), playerData.getString("password"));

                // You have to read the result first before checking if it's null
                boolean tempBool = playerData.getBoolean("tls");
                if (!playerData.wasNull())
                    mqttSettings.setTls(tempBool);
            }
        } catch(SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD +
                    translator.getString("lectern_failed_database_retrieval"));

            Logger.printException(exception);
        }
    }
}
