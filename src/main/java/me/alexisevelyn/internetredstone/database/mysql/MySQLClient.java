package me.alexisevelyn.internetredstone.database.mysql;

import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.sql.*;
import java.util.UUID;

/* Question
 * Do I really need this to be an object?
 *
 * Given that I store connection information that can only be created by initialization, I'll keep it as an object for now.
 */
public class MySQLClient {
    Connection connection;

    public MySQLClient() {
        // TODO: Create database to store registered lecterns in as well as player preferences

        String url = "jdbc:mysql://localhost:3306/internetredstone?useSSL=false";
        String user = "internetredstone";

        // As you can tell, this is not a real password. This is one that goes to a localhost
        // development database. I use this database to test Minecraft plugins and other programs that need MySQL
        String password = "localhost-dev-password";

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
                Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                        + "MySQL Version: "
                        + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                        + resultSet.getString(1));
            }
        } catch (SQLException exception) {
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                            + "SQL State: "
                            + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                            + exception.getSQLState());

            Logger.printException(exception);
        } catch (ClassNotFoundException exception) {
            // This should never run as long as the MySQL package is not shaded!!!

            // Connector/J - https://dev.mysql.com/downloads/connector/j/
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Could Not Find JDBC Driver 'com.mysql.jdbc.Driver': "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + exception.getMessage());

            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Do you not have Connector/J installed? Download it at: "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + "https://dev.mysql.com/downloads/connector/j/");

            Logger.printException(exception);
        }

        // Create MySQL Tables if Not Already Existing
        createTablesIfNotExists();
    }

    public void createTablesIfNotExists() {
        // Create the tables if not already existing

        try {
            Integer playersTable = createPlayersTable();
            Integer lecternsTable = createLecternsTable();

            Logger.finest("MySQL Result For Player Table Creation: " + playersTable);
            Logger.finest("MySQL Result For Lectern Table Creation: " + lecternsTable);

        } catch (SQLException exception) {
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Tables Creation SQL State: "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + exception.getSQLState());

            Logger.printException(exception);
        }
    }

    public Integer createPlayersTable() throws SQLException {
        /* MySQL Query - Pasted Here For Easy Modification

            CREATE TABLE IF NOT EXISTS `Players` (
                `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation',
                `broker` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user broker settings if defined. If null, default to server broker!',
                `username` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user username settings if defined. If null, default to server broker! Also, is required if using custom broker.',
                `password` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user password settings if defined. If null, default to server broker! Also, is required if using custom broker.',
                `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\'s uuid to help associate player preferences with player objects.',
                `lastKnownIGN` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\'s name to help provide a convenient mqtt link for said player to use. Never use the username to track the player as the username can change at any moment. This exists just for the players\' convenience and must be updated on player login and every once in a while to keep convenient links convenient.',
                `numberOfLecternsRegistered` INT NOT NULL DEFAULT '0' COMMENT 'Used for player statistics. Tracks the number of registered lecterns a player owns.',
                PRIMARY KEY (`entry`)
            );
         */

        String query = "CREATE TABLE IF NOT EXISTS `Players` (\n" +
                "                `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation',\n" +
                "                `broker` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user broker settings if defined. If null, default to server broker!',\n" +
                "                `username` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user username settings if defined. If null, default to server broker! Also, is required if using custom broker.',\n" +
                "                `password` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Used to store custom user password settings if defined. If null, default to server broker! Also, is required if using custom broker.',\n" +
                "                `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\\'s uuid to help associate player preferences with player objects.',\n" +
                "                `lastKnownIGN` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\\'s name to help provide a convenient mqtt link for said player to use. Never use the username to track the player as the username can change at any moment. This exists just for the players\\' convenience and must be updated on player login and every once in a while to keep convenient links convenient.',\n" +
                "                `numberOfLecternsRegistered` INT NOT NULL DEFAULT '0' COMMENT 'Used for player statistics. Tracks the number of registered lecterns a player owns.',\n" +
                "                PRIMARY KEY (`entry`)\n" +
                "            );";

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

        String query = "CREATE TABLE IF NOT EXISTS `Lecterns` (\n" +
                "                `entry` INT NOT NULL AUTO_INCREMENT COMMENT 'Used to help order entries by creation',\n" +
                "                `uuid` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Used to store player\\'s uuid to help associate lectern with player object.',\n" +
                "                `x` DECIMAL NOT NULL COMMENT 'Lectern\\'s X Coordinate',\n" +
                "                `y` DECIMAL NOT NULL COMMENT 'Lectern\\'s Y Coordinate',\n" +
                "                `z` DECIMAL NOT NULL COMMENT 'Lectern\\'s Z Coordinate',\n" +
                "                `worldUID` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'World lectern is stored in. Stored as a uuid so even if the world is renamed, the uid remains the same. Internally handled as a UUID.',\n" +
                "                `lecternID` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID used to identify sub-topic lectern should communicate on for MQTT.',\n" +
                "                `lastKnownRedstoneSignal` INT NOT NULL DEFAULT '0' COMMENT 'Last known redstone signal as recorded during plugin shutdown. Helps with preventing sending duplicate redstone signals. Can be a value from 0 to 15.',\n" +
                "                `messagesSent` BIGINT NOT NULL DEFAULT '0' COMMENT 'Number of messages sent to MQTT server for this specific lectern. Useful for statistics.',\n" +
                "                `messagesReceived` BIGINT NOT NULL DEFAULT '0' COMMENT 'Number of messages received from MQTT server for this specific lectern. Useful for statistics.',\n" +
                "                PRIMARY KEY (`entry`)\n" +
                "            );";

        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }

    public void registerLectern(UUID player, Location lectern, String lecternID, Integer lastKnownRedstoneSignal) {
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


    }

    public void unregisterLectern() {
        /* Info Needed
         *
         * Player UUID/IGN
         * Lectern Coordinates
         * Lectern World
         *
         * Lectern ID (For MQTT) - Can be set by user, must be persistent
         */

        // Lectern ID can be used to notify user that lectern was destroyed/disabled
        // Entry will be erased from Database, not just marked as disabled
    }

    public void storeUserPreferences(String broker, String username, String password, UUID player, String ign, Integer numberofLecternsRegistered) {
        // TODO: Figure out how to format this and if should split into multiple functions

        /* Info Needed
         *
         * Broker or null if using default
         * Player Database username or null if using default
         * Player Database password or null if using default
         *
         * Player UUID/IGN
         * Number of Lecterns Registered (If Implemented)
         *
         * Should I store information such as QOS/etc...?
         */
    }
}
