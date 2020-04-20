package me.alexisevelyn.internetredstone.utilities;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import me.alexisevelyn.internetredstone.utilities.data.DisconnectReason;
import me.alexisevelyn.internetredstone.utilities.data.LecternTracker;
import me.alexisevelyn.internetredstone.utilities.data.MQTTSettings;
import me.alexisevelyn.internetredstone.utilities.data.PlayerSettings;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.NotEnoughPages;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.hashids.Hashids;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.UUID;
import java.util.function.Consumer;

public class LecternHandler extends LecternTracker {
    // Used to store server's translation
    private Translator translator;

    // Settings for MQTT Client
    private MQTTSettings mqttSettings;

    // MySQL Client
    private MySQLClient mySQLClient;

    // Hash IDS
    private Hashids hashids;

    public LecternHandler(@NotNull Main main, Location location, PlayerSettings player) {
        // This is to be called on player registration
        setMain(main);
        setLocation(location);
        setPlayer(player);

        translator = main.getServerTranslator();
        mqttSettings = new MQTTSettings();
        mySQLClient = main.getMySQLClient();

        // Grab Hash From Config Or If Failed, Generate One Randomly
        hashids = new Hashids(getMain().getConfiguration().getConfig().getString("lectern.id-hash", UUID.randomUUID().toString()));

        try {
            ArrayList<MqttTopicImpl> topics = loadLecternProperties();

            /* Subscribe to Topics through ArrayList method
             *
             * NOTE: For those wondering, why use thenAccept, it's to force the subscription to wait until a connection has been established.
             */
            getClient().getConnection()
                    .thenAccept(read -> getClient().subscribe(topics, callback))
                    .thenAccept(mysql -> addDatabaseEntry());
        } catch (ConnectionFailedException | ConnectionClosedException | Mqtt5ConnAckException exception) {
            Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("mqtt_failed_connection"));
            Logger.printException(exception);
        } catch (MqttClientStateException exception) {
            Logger.warning(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("mqtt_already_connecting"));
        }
    }

    public void sendRedstoneUpdate(Integer signal) {
        // Sending Update Means Last Known Power Needs To Be Set
        setLastKnownPower(signal);

        // Convert Integer to byte array for MQTT Client to Process
        byte[] payload = signal.toString().getBytes();

        // Send Message To Registered Topics
        for (MqttTopicImpl topic : mqttSettings.getTopics()) {
            getClient().sendMessage(topic, payload);
        }
    }

    private ArrayList<MqttTopicImpl> loadLecternProperties() {
        // Retrieve Player's UUID
        UUID player = getPlayer().getUUID();

        // This string will be read from Player's Config Or If None Provided, Server's Config
        FileConfiguration config = getMain().getConfiguration().getConfig();

        // Connection Settings
        mqttSettings.setBroker(config.getString("default.broker"));
        mqttSettings.setPort(config.getInt("default.port", 1883));
        mqttSettings.setTls(config.getBoolean("default.tls", false));

        // Authentication
        mqttSettings.setSimpleAuth(config.getString("default.username", null),
                config.getString("default.password", null));

        String server_name = config.getString("server-name");

        // Determine if Should Send Retained Messages Or Not
        mqttSettings.setRetainMessage(config.getBoolean("default.retain", true));


        ResultSet lecternData;
        ResultSet playerData;
        try {
            lecternData = mySQLClient.retrieveLecternDataIfExists(getLocation());

            if (lecternData != null && lecternData.next()) {
                setLecternID(lecternData.getString("lecternID"));
                setLastKnownPower(lecternData.getInt("lastKnownRedstoneSignal"));
            }

            playerData = mySQLClient.retrievePlayerDataIfExists(player);
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

        // Checks if String Was Previously Set Because Of MySQL Data
        // Attempt to retrieve id from database, or if failed, then generate by other means
        if (StringUtils.isBlank(getLecternID())) {
            // Temporary Means of Generating Semi-Unique IDs for Testing
            int tries = getMain().getConfiguration().getConfig().getInt("lectern.generate-short-id-tries", 3);
            int maxShortID = getMain().getConfiguration().getConfig().getInt("lectern.max-short-id", 10000);
            boolean success = false;

            SecureRandom random = new SecureRandom();
            int chosenID;
            for (int x = 0; x <= tries; x++) {
                // Generate a Random Integer For Checking Against Database
                chosenID = random.nextInt(maxShortID);

                try {
                    // If random integer is not being used, use it, otherwise try again
                    if (!mySQLClient.isLecternIDUsed(String.valueOf(chosenID))) {
                        setLecternID(hashids.encode(chosenID));
                        success = true;

                        break;
                    }
                } catch (SQLException exception) {
                    // Failed to check against database, reverting to uuid method of id generation
                    Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_check_id_sql_exception"));
                    Logger.printException(exception);

                    break;
                }
            }

            // If failed to find a short and sweet id for the lectern,
            // then just generate a Universally Unique Identifier
            if (!success) {
                setLecternID(UUID.randomUUID().toString());
            }
        }

        // List of Topics to Subscribe To
        mqttSettings.addTopic(server_name + "/" + player + "/" + getLecternID()); // Topic based on player's uuid

        // Get Player's Name if Possible, Otherwise, Just Stick With UUID
        // This should work regardless if the player is online or not, so long as we have seen them before.
        OfflinePlayer playerObject = Bukkit.getOfflinePlayer(player);

        // Make sure to not set the ign topic to "null"
        if (StringUtils.isNotBlank(playerObject.getName())) {
            mqttSettings.addTopic(server_name + "/" + playerObject.getName() + "/" + getLecternID()); // Topic based on player's ign
        }

        // Create MQTT Client For Lectern
//        setClient(new MQTTClient(getBroker(), getPort(), getTls(), lwt));

        // Client With Username/Password (TODO: Test if Works With Null Values)
        setClient(new MQTTClient(mqttSettings));

        return mqttSettings.getTopics();
    }

    private void addDatabaseEntry() {
        // The Runnable is to make sure MySQL is called synchronously as all trackers share the same connection.
        Bukkit.getScheduler().runTask(getMain(), () -> {
            try {
                // Update Number of Registered Lecterns

                // Broker is set to null as user can set broker via commands. Same for username and password.
                // The player data won't be overwritten by setting it again. The same goes for the lectern.
                // Also, we are going to use the default settings provided by the server owner if it's null in the database.
                Player player = Bukkit.getPlayer(getPlayer().getUUID());

                // If Player object is null (e.g. player is offline), then set Locale to null, otherwise set the player's locale
                if (player != null)
                    mySQLClient.storeUserPreferences(null, null, null, getPlayer().getUUID(), player.getLocale());
                else
                    mySQLClient.storeUserPreferences(null, null, null, getPlayer().getUUID(), null);

                mySQLClient.registerLectern(getPlayer().getUUID(), getLocation(), getLecternID(), getLastKnownPower());
            } catch (SQLException exception) {
                Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_failed_add_entries_sql_exception"));
                Logger.printException(exception);
            }
        });
    }

    public void unregister(DisconnectReason disconnectReason) {
        Bukkit.getScheduler().runTask(getMain(), () -> {
            try {
                // Update Number of Registered Lecterns
                //mySQLClient.storeUserPreferences(client.getBroker(), null, null, getPlayer(), null, 0);

                mySQLClient.unregisterLectern(getLocation());
            } catch (SQLException exception) {
                Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_failed_remove_entries_sql_exception"));
                Logger.printException(exception);
            }

            cleanup(disconnectReason);
        });
    }

    // Function To Run Asynchronously When Message is Received
    final Consumer<Mqtt5Publish> callback = mqtt5Publish -> {
        String decoded = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

        Logger.info(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                + translator.getString("lectern_received_message")
                + ChatColor.DARK_PURPLE + ChatColor.BOLD
                + mqtt5Publish.getTopic().toString());

        Logger.info(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                + translator.getString("lectern_message")
                + ChatColor.AQUA + ChatColor.BOLD
                + decoded
                + ChatColor.RESET);

        Integer powerLevel;
        try {
            powerLevel = Integer.parseInt(decoded) - 1;
        } catch (NumberFormatException exception) {
            return;
        }

        if (0 <= powerLevel && powerLevel <= 14) {
            Bukkit.getScheduler().runTask(getMain(), () -> {
                Logger.info(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + translator.getString("lectern_setting_redstone_signal")
                        + ChatColor.AQUA + ChatColor.BOLD + (powerLevel + 1));

                try {
                    BlockState snapshot = getLocation().getBlock().getState();

                    if (snapshot instanceof Lectern) {
                        Lectern lectern = (Lectern) snapshot;

                        Logger.info(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                                + translator.getString("lectern_current_page")
                                + ChatColor.AQUA + ChatColor.BOLD
                                + lectern.getPage() + 1
                                + ChatColor.RESET);

                        // Update First Page In Book
                        // Must be performed before setting the page number!!!
                        writeLastMessage(lectern, mqtt5Publish, powerLevel);

                        try {
                            validateBook(lectern);
                        } catch (InvalidBook exception) {
                            // Just return!!!
                            Logger.warning(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                                    + translator.getString("lectern_failed_retrieve_book")
                                    + exception.getMessage());

                            return;
                        } catch (NotEnoughPages exception) {
                            String warning = String.valueOf(ChatColor.DARK_RED) + ChatColor.BOLD
                                    + exception.getMessage();

                            updateMainPage(lectern, warning);
                            snapshot.update();

                            return;
                        }

                        lectern.setPage(powerLevel);

                        snapshot.update();
                    }
                } catch(NullPointerException exception) {
                    Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                            + translator.getString("lectern_failed_set_power_level_npe")
                            + ChatColor.AQUA + ChatColor.BOLD
                            + (powerLevel + 1)
                            + ChatColor.RESET);

                    Logger.severe(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                            + translator.getString("lectern_update_latest_server"));

                    Logger.printException(exception);
                }
            });
        }
    };

    public void validateBook(Lectern lectern) throws InvalidBook, NotEnoughPages {
        LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

        ItemStack book;
        BookMeta bookMeta;

        book = LecternUtilities.getItem(inventory);
        bookMeta = LecternUtilities.getBookMeta(book);

        if (bookMeta.getPageCount() != 15) {
            String warning;
            try {
                warning = String.format(translator.getString("lectern_not_enough_pages"), bookMeta.getPageCount());
            } catch (IllegalFormatException ignored) {
                // This is in case someone forgets to put %s in the translation!!!
                warning = translator.getString("lectern_not_enough_pages");
            }

            throw new NotEnoughPages(warning);
        }
    }

    public void writeLastMessage(Lectern lectern, Mqtt5Publish mqtt5Publish, Integer powerLevel) {
        String page = String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD +
                // last message
                getPlayer().getTranslator().getString("lectern_last_message") +
                ChatColor.DARK_RED + ChatColor.BOLD +
                (powerLevel + 1) + "\n" +

                // from channel
                ChatColor.DARK_GREEN + ChatColor.BOLD +
                getPlayer().getTranslator().getString("lectern_from_channel") +
                ChatColor.DARK_RED + ChatColor.BOLD +
                mqtt5Publish.getTopic().toString();

        updateMainPage(lectern, page);
    }

    public void updateMainPage(Lectern lectern, String page) {
        LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

        ItemStack book;
        BookMeta bookMeta;

        book = LecternUtilities.getItem(inventory);
        try {
            bookMeta = LecternUtilities.getBookMeta(book);
        } catch (InvalidBook exception) {
            Logger.warning(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD
                    + translator.getString("lectern_lectern_handler")
                    + exception.getMessage());

            return;
        }

        bookMeta.setPage(1, String.valueOf(ChatColor.DARK_PURPLE) + ChatColor.BOLD
                + LecternUtilities.getIdentifier()
                + "\n"
                + page);

        book.setItemMeta(bookMeta);
        inventory.setItem(0, book);
    }

    @Override
    public void cleanup(DisconnectReason disconnectReason) {
        /*
         * Disconnect MQTT Client Properly
         *
         * (that way the MQTT Last Will and Testament isn't executed
         * and can be replaced by a server shutdown message
         * or a tracker destroyed message)
         */

        byte[] payload;
        switch (disconnectReason.getReason()) {
            case BROKEN_LECTERN:
                payload = getPlayer().getTranslator().getString("lectern_disconnect_broken_lectern").getBytes();
                break;
            case OTHER:
                payload = getPlayer().getTranslator().getString("lectern_disconnect_other").getBytes();
                break;
            case REMOVED_BOOK:
                payload = getPlayer().getTranslator().getString("lectern_disconnect_removed_book").getBytes();
                break;
            case SERVER_SHUTDOWN:
                payload = getPlayer().getTranslator().getString("lectern_disconnect_server_shutdown").getBytes();
                break;
            default:
                payload = getPlayer().getTranslator().getString("lectern_disconnect_uncaught").getBytes();
        }

        for (MqttTopicImpl topic : mqttSettings.getTopics()) {
            getClient().sendMessage(topic, payload);
        }

        getClient().disconnect();

        // Don't handle MySQL Client here as one instance is shared across all trackers!!!
    }
}
