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

    public PlayerSettings(UUID uuid, Main main) {
        this.UUID = uuid;
        this.main = main;

        this.mySQLClient = main.getMySQLClient();
    }
}
