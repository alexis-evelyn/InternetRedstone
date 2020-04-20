package me.alexisevelyn.internetredstone.utilities.handlers;

import lombok.Data;
import lombok.SneakyThrows;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.Translator;
import me.alexisevelyn.internetredstone.utilities.data.PlayerSettings;
import me.alexisevelyn.internetredstone.utilities.exceptions.DuplicateObjectException;
import me.alexisevelyn.internetredstone.utilities.exceptions.MissingObjectException;
import org.bukkit.ChatColor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IllegalFormatException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PlayerHandlers {
    // Main class used to get synchronous execution
    final private Main main;

    // MySQL Instance
    final MySQLClient mySQLClient;

    // Server Translation Language
    final private Translator translator;

    // List of Tracker Objects
    final ConcurrentHashMap<UUID, PlayerSettings> handlers;

    public PlayerHandlers(Main main) {
        this.main = main;
        this.mySQLClient = main.getMySQLClient();
        this.translator = main.getServerTranslator();

        handlers = new ConcurrentHashMap<>();

        // Register Saved Handlers From Database
        registerSavedHandlers();
    }

    public void registerSavedHandlers() {
        MySQLClient mySQLClient = main.getMySQLClient();

        try {
            ResultSet playersInfo = mySQLClient.retrieveAllRegisteredPlayersIfExists();
            UUID player_uuid;

            while (playersInfo.next()) {
                player_uuid = UUID.fromString(playersInfo.getString("uuid"));

                registerHandler(player_uuid);
            }

        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_startup_failed_register_all"));
            Logger.printException(exception);
        }
    }

    @SneakyThrows
    public void registerHandler(UUID player_uuid) {
        if (handlers.containsKey(player_uuid)) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_already_registered_warning"),
                        player_uuid.toString() + ChatColor.GOLD + ChatColor.BOLD);
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_already_registered_warning");
            }

            throw new DuplicateObjectException(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + warning);
        }

        PlayerSettings player = new PlayerSettings(player_uuid, main);
        handlers.put(player_uuid, player);
    }

    @SneakyThrows
    @Deprecated // Not Really Sure If I'm Going to Keep This
    public void unregisterHandler(UUID player_uuid) {
        if (!handlers.containsKey(player_uuid)) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_missing_warning"),
                        player_uuid.toString() + ChatColor.GOLD + ChatColor.BOLD);
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_missing_warning");
            }

            throw new MissingObjectException(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + warning);
        }

        // TODO: Remove Saved Lecterns Here?

        handlers.remove(player_uuid);
    }

    public boolean isRegistered(UUID player_uuid) {
        return handlers.containsKey(player_uuid);
    }

    @SneakyThrows
    public PlayerSettings getHandler(UUID player_uuid) {
        if (!isRegistered(player_uuid)) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_missing_warning"),
                        player_uuid.toString() + ChatColor.GOLD + ChatColor.BOLD);
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_missing_warning");
            }

            throw new MissingObjectException(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + warning);
        }


        return handlers.get(player_uuid);
    }

    public ConcurrentHashMap<UUID, PlayerSettings> getHandlers() {
        return handlers;
    }

    public void updatePlayerLocale(UUID player_uuid, String locale) throws SQLException {
        // Update Locale in MySQL Database
        mySQLClient.updateLocale(player_uuid, locale);

        // Update Locale in Player Settings Data Class
        getHandler(player_uuid).setLocale(locale);
    }
}
