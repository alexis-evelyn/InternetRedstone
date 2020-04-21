package me.alexisevelyn.internetredstone.database.mysql;

import com.axiomalaska.jdbc.NamedParameterPreparedStatement;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.Translator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.UUID;

/* Question
 * Do I really need this to be an object?
 *
 * Given that I store connection information that can only be created by initialization, I'll keep it as an object for now.
 */
public class MySQLClient {
    Connection connection;

    final Translator translator;

    public MySQLClient(Main main) {
        FileConfiguration config = main.getConfiguration().getConfig();

        translator = main.getServerTranslator();

        String url = config.getString("mysql.url");
        String user = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        if (StringUtils.isBlank(url) || StringUtils.isBlank(user) || StringUtils.isBlank(password) ) {
            Logger.severe(String.valueOf(ChatColor.GOLD) +
                    ChatColor.DARK_PURPLE +
                    translator.getString("mysql_failed_missing_essentials"));

            // Disable Our Own Plugin Since Data is Missing From Config!!!
            Bukkit.getPluginManager().disablePlugin(main);
            return;
        }

        String query = "SELECT VERSION()";

        try {
            // Load and Register the MySQL JDBC Driver If Available
            // This shouldn't be necessary anymore, but it won't hurt having this
            Class.forName("com.mysql.jdbc.Driver");

            // Setup MySQL Connection
            connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                Logger.info(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                        + translator.getString("mysql_version")
                        + ChatColor.DARK_PURPLE + ChatColor.BOLD
                        + resultSet.getString(1));
            }
        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                            + translator.getString("mysql_state")
                            + ChatColor.DARK_PURPLE + ChatColor.BOLD
                            + exception.getSQLState());

            Logger.printException(exception);

            // Disable Our Own Plugin Since Something is Wrong With the MySQL Connection/Settings
            Bukkit.getPluginManager().disablePlugin(main);
            return;
        } catch (ClassNotFoundException exception) {
            // This should never run as long as the MySQL package is not shaded!!!

            // Connector/J - https://dev.mysql.com/downloads/connector/j/
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("mysql_no_find_jdbc_driver")
                    + ChatColor.DARK_PURPLE + ChatColor.BOLD
                    + exception.getMessage());

            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("mysql_connector_j_installed")
                    + ChatColor.DARK_PURPLE + ChatColor.BOLD
                    + "https://dev.mysql.com/downloads/connector/j/");

            Logger.printException(exception);

            // Disable Our Own Plugin Since MySQL is Missing!!!
            Bukkit.getPluginManager().disablePlugin(main);
            return;
        }

        // Create MySQL Tables if Not Already Existing
        createTablesIfNotExists();
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("mysql_disconnect_exception")
                    + ChatColor.DARK_PURPLE + ChatColor.BOLD
                    + exception.getSQLState());

            Logger.printException(exception);
        }
    }

    public void createTablesIfNotExists() {
        // Create the tables if not already existing

        try {
            Integer playersTable = createPlayersTable();
            Integer lecternsTable = createLecternsTable();

            Logger.finest(translator.getString("mysql_log_player_table_creation") + playersTable);
            Logger.finest(translator.getString("mysql_log_lectern_table_creation") + lecternsTable);

        } catch (SQLException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("mysql_create_tables_exception")
                    + ChatColor.DARK_PURPLE + ChatColor.BOLD
                    + exception.getSQLState());

            Logger.printException(exception);
        }
    }

    public Integer createPlayersTable() throws SQLException {
        /* MySQL Query - Pasted Here For Easy Modification

            CREATE TABLE IF NOT EXISTS `Players` (
                `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation',
                `broker` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user broker settings if defined. If null, default to server broker!',
                `port` INT DEFAULT NULL COMMENT 'Used to set port for MQTT Client to use',
                `tls` BOOLEAN DEFAULT NULL COMMENT 'Used to determine if tls will be used for MQTT Client',
                `username` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user username settings if defined. If null, default to server broker! Also, is required if using custom broker.',
                `password` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user password settings if defined. If null, default to server broker! Also, is required if using custom broker.',
                `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\'s uuid to help associate player preferences with player objects.',
                `locale` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used for text translations.',
                `numberOfLecternsRegistered` INT NOT NULL DEFAULT '0' COMMENT 'Used for player statistics. Tracks the number of registered lecterns a player owns.',
                PRIMARY KEY (`entry`)
            );
         */

        String query = "CREATE TABLE IF NOT EXISTS `Players` (" +
                " `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation'," +
                " `broker` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user broker settings if defined. If null, default to server broker!'," +
                " `port` INT DEFAULT NULL COMMENT 'Used to set port for MQTT Client to use'," +
                " `tls` BOOLEAN DEFAULT NULL COMMENT 'Used to determine if tls will be used for MQTT Client'," +
                " `username` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user username settings if defined. If null, default to server broker! Also, is required if using custom broker.'," +
                " `password` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user password settings if defined. If null, default to server broker! Also, is required if using custom broker.'," +
                " `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\\'s uuid to help associate player preferences with player objects.'," +
                " `locale` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used for text translations.'," +
                " `numberOfLecternsRegistered` INT NOT NULL DEFAULT '0' COMMENT 'Used for player statistics. Tracks the number of registered lecterns a player owns.'," +
                " PRIMARY KEY (`entry`));";

        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }

    public Integer createLecternsTable() throws SQLException {
        /* MySQL Query - Pasted Here For Easy Modification

            CREATE TABLE IF NOT EXISTS `Lecterns` (
                `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation',
                `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\'s uuid to help associate lectern with player object.',
                `x` DECIMAL NOT NULL COMMENT 'Lectern\'s X Coordinate',
                `y` DECIMAL NOT NULL COMMENT 'Lectern\'s Y Coordinate',
                `z` DECIMAL NOT NULL COMMENT 'Lectern\'s Z Coordinate',
                `worldUID` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'World lectern is stored in. Stored as a uuid so even if the world is renamed, the uid remains the same. Internally handled as a UUID.',
                `lecternID` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID used to identify sub-topic lectern should communicate on for MQTT.',
                `lastKnownRedstoneSignal` INT NOT NULL DEFAULT '0' COMMENT 'Last known redstone signal as recorded during plugin shutdown. Helps with preventing sending duplicate redstone signals. Can be a value from 0 to 15.',
                `messagesSent` BIGINT NOT NULL DEFAULT '0' COMMENT 'Number of messages sent to MQTT server for this specific lectern. Useful for statistics.',
                `messagesReceived` BIGINT NOT NULL DEFAULT '0' COMMENT 'Number of messages received from MQTT server for this specific lectern. Useful for statistics.',
                PRIMARY KEY (`entry`)
            );
         */

        String query = "CREATE TABLE IF NOT EXISTS `Lecterns` (" +
                " `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation'," +
                " `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\\'s uuid to help associate lectern with player object.'," +
                " `x` DECIMAL NOT NULL COMMENT 'Lectern\\'s X Coordinate'," +
                " `y` DECIMAL NOT NULL COMMENT 'Lectern\\'s Y Coordinate'," +
                " `z` DECIMAL NOT NULL COMMENT 'Lectern\\'s Z Coordinate'," +
                " `worldUID` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'World lectern is stored in. Stored as a uuid so even if the world is renamed, the uid remains the same. Internally handled as a UUID.'," +
                " `lecternID` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID used to identify sub-topic lectern should communicate on for MQTT.'," +
                " `lastKnownRedstoneSignal` INT NOT NULL DEFAULT '0' COMMENT 'Last known redstone signal as recorded during plugin shutdown. Helps with preventing sending duplicate redstone signals. Can be a value from 0 to 15.'," +
                " `messagesSent` BIGINT NOT NULL DEFAULT '0' COMMENT 'Number of messages sent to MQTT server for this specific lectern. Useful for statistics.'," +
                " `messagesReceived` BIGINT NOT NULL DEFAULT '0' COMMENT 'Number of messages received from MQTT server for this specific lectern. Useful for statistics.'," +
                " PRIMARY KEY (`entry`));";

        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }

    public void registerLectern(UUID player, Location lectern, String lecternID, Integer lastKnownRedstoneSignal) throws SQLException {
        /* Info Needed
         *
         * Player UUID (UUID player)
         * Lectern Coordinates (Location lectern)
         * Lectern World (Location lectern)
         *
         * Lectern ID (For MQTT) - Can be set by user, must be persistent (String lecternID)
         * Last Known Redstone Signal (If Implemented) (Integer lastKnownRedstoneSignal)
         *
         * Number of Messages Sent (If Implemented)
         * Number of Messages Received (If Implemented)
         */

        // Check if Lectern is Already in Database
        if (isLecternInDatabase(lectern))
            return;

        // The row shouldn't already exist if we are here, so we insert it
        // UUID player, Location lectern, String lecternID, Integer lastKnownRedstoneSignal

        String query = "INSERT INTO Lecterns" +
                " (uuid, x, y, z, worldUID, lecternID, lastKnownRedstoneSignal) VALUES" +
                " (:uuid, :x, :y, :z, :world, :lecternID, :signal)";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("uuid", player.toString());

        preparedStatement.setString("x", String.valueOf(lectern.getBlockX()));
        preparedStatement.setString("y", String.valueOf(lectern.getBlockY()));
        preparedStatement.setString("z", String.valueOf(lectern.getBlockZ()));

        preparedStatement.setString("world", lectern.getWorld().getUID().toString());

        preparedStatement.setString("lecternID", lecternID);

        preparedStatement.setString("signal", lastKnownRedstoneSignal.toString());

        preparedStatement.executeUpdate();
    }

    public void unregisterLectern(Location lectern) throws SQLException {
        /* Info Needed
         *
         * Player UUID
         * Lectern Coordinates
         * Lectern World
         *
         * Lectern ID (For MQTT) - Can be set by user, must be persistent
         */

        // Lectern ID can be used to notify user that lectern was destroyed/disabled
        // Entry will be erased from Database, not just marked as disabled

        /* MySQL Query - Pasted Here For Easy Modification

            DELETE
            FROM Lecterns
            WHERE 'x' = 1 and `y` = 2 and `z` = 3 and `worldUID` = 'hello';
         */

        // Should I Put a Limit 1 Here?
        String query = "DELETE FROM Lecterns" +
                " WHERE `x` = :x and `y` = :y and `z` = :z and `worldUID` = :world LIMIT 1;";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("x", String.valueOf(lectern.getBlockX()));
        preparedStatement.setString("y", String.valueOf(lectern.getBlockY()));
        preparedStatement.setString("z", String.valueOf(lectern.getBlockZ()));

        preparedStatement.setString("world", lectern.getWorld().getUID().toString());

        preparedStatement.executeUpdate();
    }

    public void storeUserPreferences(String broker, String username, String password, UUID player, @Nullable String locale) throws SQLException {
        /* Info Needed
         *
         * Broker or null if using default
         * Player Database username or null if using default
         * Player Database password or null if using default
         *
         * Player UUID
         * Number of Lecterns Registered (If Implemented)
         *
         * Should I store information such as QOS/etc...?
         */

        // Check if Lectern is Already in Database
        if (isPlayerInDatabase(player))
            return;

        // The row shouldn't already exist if we are here, so we insert it
        // UUID player, Location lectern, String lecternID, Integer lastKnownRedstoneSignal

        String query = "INSERT INTO Players" +
                " (broker, username, password, uuid, locale) VALUES" +
                " (:broker, :username, :password, :uuid, :locale)";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("broker", broker);

        preparedStatement.setString("username", username);
        preparedStatement.setString("password", password);

        preparedStatement.setString("uuid", player.toString());

        preparedStatement.setString("locale", locale);

        preparedStatement.executeUpdate();
    }

    private boolean isLecternInDatabase(Location lectern) throws SQLException {
        String query = "SELECT EXISTS(SELECT entry FROM Lecterns" +
                " WHERE `x` = :x and `y` = :y and `z` = :z and `worldUID` = :world LIMIT 1);";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("x", String.valueOf(lectern.getBlockX()));
        preparedStatement.setString("y", String.valueOf(lectern.getBlockY()));
        preparedStatement.setString("z", String.valueOf(lectern.getBlockZ()));

        preparedStatement.setString("world", lectern.getWorld().getUID().toString());

        ResultSet doesExist = preparedStatement.executeQuery();

        // If result is 1, then lectern is already registered, otherwise, if 0, it's not
        return doesExist.next() && doesExist.getInt(1) == 1;
    }

    private boolean isPlayerInDatabase(UUID player) throws SQLException {
        String query = "SELECT EXISTS(SELECT entry FROM Players" +
                " WHERE `uuid` = :uuid LIMIT 1);";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("uuid", player.toString());

        ResultSet doesExist = preparedStatement.executeQuery();

        // If result is 1, then lectern is already registered, otherwise, if 0, it's not
        return doesExist.next() && doesExist.getInt(1) == 1;
    }

    public ResultSet getNumberOfRegisteredLecterns() throws SQLException {
        String query = "SELECT COUNT(*) FROM Lecterns;";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeQuery();
    }

    public ResultSet getNumberOfRegisteredPlayers() throws SQLException {
        String query = "SELECT COUNT(*) FROM Players;";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeQuery();
    }

    public ResultSet retrieveLecternDataIfExists(Location lectern) throws SQLException {
        if (!isLecternInDatabase(lectern))
            return null;

        String query = "SELECT uuid, lecternID, lastKnownRedstoneSignal, messagesSent, messagesReceived FROM Lecterns" +
                " WHERE `x` = :x and `y` = :y and `z` = :z and `worldUID` = :world LIMIT 1;";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("x", String.valueOf(lectern.getBlockX()));
        preparedStatement.setString("y", String.valueOf(lectern.getBlockY()));
        preparedStatement.setString("z", String.valueOf(lectern.getBlockZ()));

        preparedStatement.setString("world", lectern.getWorld().getUID().toString());

        return preparedStatement.executeQuery();
    }

    public ResultSet retrievePlayerDataIfExists(UUID player) throws SQLException {
        if (!isPlayerInDatabase(player))
            return null;

        String query = "SELECT broker, port, tls, username, password, numberOfLecternsRegistered, locale FROM Players" +
                " WHERE `uuid` = :uuid LIMIT 1;";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("uuid", player.toString());

        return preparedStatement.executeQuery();
    }

    public ResultSet retrieveAllRegisteredLecternsIfExists() throws SQLException {
        String query = "SELECT uuid, x, y, z, worldUID FROM Lecterns;";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        return preparedStatement.executeQuery();
    }

    // Used to Check If Lectern ID is Already Used On New Registration
    public Boolean isLecternIDUsed(String uuid) throws SQLException {
        String query = "SELECT EXISTS(SELECT entry FROM Lecterns" +
                " WHERE `lecternID` = :lecternID LIMIT 1);";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("lecternID", uuid);

        ResultSet doesExist = preparedStatement.executeQuery();

        // If result is 1, then lectern is already registered, otherwise, if 0, it's not
        return doesExist.next() && doesExist.getInt(1) == 1;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Boolean updateLocale(UUID player, String locale) throws SQLException {
        if (!isPlayerInDatabase(player))
            return false;

        String query = "UPDATE Players"
                + " SET locale = :locale"
                + " WHERE `uuid` = :uuid LIMIT 1;";

        NamedParameterPreparedStatement preparedStatement = NamedParameterPreparedStatement
                .createNamedParameterPreparedStatement(connection, query);

        preparedStatement.setString("uuid", player.toString());
        preparedStatement.setString("locale", locale);

        return preparedStatement.executeUpdate() > 0;
    }

    public void setBroker(UUID player, @NotNull String broker) {
    }

    public void setPort(UUID player, Integer port) {

    }

    public void setTLS(UUID player, Boolean tls) {

    }

    public void setSimpleAuth(UUID player, @NotNull String username, @Nullable String password) {

    }
}
