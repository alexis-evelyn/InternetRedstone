package me.alexisevelyn.internetredstone.utilities;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
import me.alexisevelyn.internetredstone.utilities.exceptions.NotEnoughPages;
import me.alexisevelyn.internetredstone.utilities.abstracted.Tracker;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class LecternTracker extends Tracker {
    String lecternID;
    String topic_uuid;
    String topic_ign;

    MQTTClient client;
    final MySQLClient mySQLClient;

    public LecternTracker(@NotNull Main main, Location location, UUID player) {
        setLocation(location);
        setPlayer(player);
        setMain(main);

        mySQLClient = main.getMySQLClient();

        try {
            ArrayList<String> topics = loadLecternProperties();

            /* Subscribe to Topics through ArrayList method
             *
             * NOTE: For those wondering, why use thenAccept, it's to force the subscription to wait until a connection has been established.
             */
            client.getConnection()
                    .thenAccept(read -> client.subscribe(topics, callback))
                    .thenAccept(mysql -> addDatabaseEntry());
        } catch (ConnectionFailedException exception) {
            Logger.severe("Failed to even send the connect message!!!");
        } catch (ConnectionClosedException exception) {
            Logger.severe("Connection closed before ConnAck could be received!!! ConnAck is the server acknowledging our connection!!!");
        } catch (Mqtt5ConnAckException exception) {
            Logger.severe("ConnAck Exception Received!!! Server sent an error response!!!");
            Logger.printException(exception);
        } catch (MqttClientStateException exception) {
            Logger.warning("Already Connected or Connecting!!!");
        }
    }

    private void setLecternID(String lecternID) {
        this.lecternID = lecternID;
    }

    private void setTopic_uuid(String topic_uuid) {
        this.topic_uuid = topic_uuid;
    }

    private void setTopic_ign(String topic_ign) {
        this.topic_ign = topic_ign;
    }

    public String getLecternID() {
        return lecternID;
    }

    public String getTopic_uuid() {
        return topic_uuid;
    }

    public String getTopic_ign() {
        return topic_ign;
    }

    public void sendRedstoneUpdate(Integer signal) {
        byte[] payload = signal.toString().getBytes();

        // Send Message To Topic With UUID
        client.sendMessage(getTopic_uuid(), payload, MqttQos.EXACTLY_ONCE);

        // Send Message To Topic With IGM - Topic may be null here
        if (getTopic_ign() != null) {
            client.sendMessage(getTopic_ign(), payload, MqttQos.EXACTLY_ONCE);
        }
    }

    private ArrayList<String> loadLecternProperties() {
        // Retrieve Player's UUID
        UUID player = getPlayer();

        // This string will be read from Player's Config Or If None Provided, Server's Config
        // TODO: Attempt to retrieve broker from database, or if failed, then load from config
        String broker = "broker.hivemq.com";

        // Temporarily Hardcoded Server Value for Testing
        // TODO: Replace with loading value from config
        String server_name = "alexis";

        ResultSet lecternData;
        try {
            lecternData = mySQLClient.retrieveLecternDataIfExists(getLocation());

            broker = "broker.hivemq.com";

            setLecternID("lecternID");
            setLastKnownPower(lecternData.getInt("lastKnownRedstoneSignal"));
        } catch(SQLException exception) {
            Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD +
                    "Failed To Retrieve Lectern Data From Database Due To SQLException!!!");

            Logger.printException(exception);
        }

        // Checks if String Was Previously Set Because Of MySQL Data
        if (StringUtils.isBlank(getLecternID())) {
            // Temporary Means of Generating Semi-Unique IDs for Testing
            // TODO: Attempt to retrieve id from database, or if failed, then generate by other means
            Random temporary = new Random();
            setLecternID(String.valueOf(temporary.nextInt(100)));
        }

        // Create MQTT Client For Lectern
        client = new MQTTClient(player, broker);

        // List of Topics to Subscribe To
        // TODO: Eventually Allow Customizing Topic String In Config
        setTopic_uuid(server_name + "/" + player + "/" + getLecternID()); // Topic based on player's uuid

        // ArrayList of Topics to Subscribe To
        ArrayList<String> topics = new ArrayList<>();
        topics.add(getTopic_uuid());

        // Get Player's Name if Possible, Otherwise, Just Stick With UUID
        Player online_player = Bukkit.getPlayer(player);
        if (online_player != null) {
            // TODO: Eventually Allow Customizing Topic String In Config
            setTopic_ign(server_name + "/" + online_player.getName() + "/" + getLecternID()); // Topic based on player's ign
            topics.add(getTopic_ign());
        }

        return topics;
    }

    private void addDatabaseEntry() {
        // TODO: Add String for username, password, and ign!!!

        // The Runnable is to make sure MySQL is called synchronously as all trackers share the same connection.
        Bukkit.getScheduler().runTask(getMain(), () -> {
            try {
                // Update Number of Registered Lecterns and IGN
                //mySQLClient.storeUserPreferences(client.getBroker(), null, null, getPlayer(), null, 0);

                mySQLClient.registerLectern(getPlayer(), getLocation(), getLecternID(), getLastKnownPower());
            } catch (SQLException exception) {
                Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                        + "Failed to add database entries in Lectern Tracker!!!");

                Logger.printException(exception);
            }
        });
    }

    public void unregister() {
        try {
            // Update Number of Registered Lecterns and IGN
            //mySQLClient.storeUserPreferences(client.getBroker(), null, null, getPlayer(), null, 0);

            mySQLClient.unregisterLectern(getLocation());
        } catch (SQLException exception) {
            Logger.severe("Failed to remove database entries in Lectern Tracker!!!");

            Logger.printException(exception);
        }

        cleanup();
    }

    // Function To Run Asynchronously When Message is Received
    final Consumer<Mqtt5Publish> callback = mqtt5Publish -> {
        String decoded = new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

        Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                + "Received Message On Topic: "
                + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                + mqtt5Publish.getTopic().toString());

        Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                + "Message: "
                + ChatColor.AQUA + "" + ChatColor.BOLD
                + decoded
                + ChatColor.RESET);

        Integer powerLevel;
        try {
            powerLevel = Integer.parseInt(decoded) - 1;
        } catch (NumberFormatException exception) {
//            Logger.info(ChatColor.GOLD + "Not A Valid Integer: "
//                    + ChatColor.DARK_PURPLE + decoded);
            return;
        }

        if (0 <= powerLevel && powerLevel <= 14) {
            Bukkit.getScheduler().runTask(getMain(), () -> {
                Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD + "Setting Redstone Signal To: "
                        + ChatColor.AQUA + "" + ChatColor.BOLD + (powerLevel + 1));

                try {
                    BlockState snapshot = getLocation().getBlock().getState();

                    if (snapshot instanceof Lectern) {
                        Lectern lectern = (Lectern) snapshot;

                        Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                                + "Current Page Is: "
                                + ChatColor.AQUA + "" + ChatColor.BOLD
                                + lectern.getPage()
                                + ChatColor.RESET);

                        // Update First Page In Book
                        // Must be performed before setting the page number!!!
                        writeLastMessage(lectern, mqtt5Publish, powerLevel);

                        try {
                            validateBook(lectern);
                        } catch (InvalidLectern | InvalidBook exception) {
                            // Just return!!!

                            Logger.warning("Failed to Get Lectern/Book: " + exception.getMessage());
                            return;
                        } catch (NotEnoughPages exception) {
                            String warning = ChatColor.DARK_RED + "" + ChatColor.BOLD
                                    + exception.getMessage();

                            updateMainPage(lectern, warning);
                            snapshot.update();
                            return;
                        }

                        lectern.setPage(powerLevel);

                        snapshot.update();
                    }
                } catch(NullPointerException exception) {
                    Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                            + "Failed To Set Redstone Signal To Due To NullPointerException With setPage(int): "
                            + ChatColor.AQUA + "" + ChatColor.BOLD
                            + (powerLevel + 1)
                            + ChatColor.RESET);

                    Logger.severe(ChatColor.GOLD + "" + ChatColor.BOLD
                            + "Update to the latest server software (either Spigot/Paper)!!!");

                    Logger.printException(exception);
                }
            });
        }
    };

    public void validateBook(Lectern lectern) throws InvalidBook, InvalidLectern, NotEnoughPages {
        LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

        ItemStack book;
        BookMeta bookMeta;

        book = LecternUtilities.getItem(inventory);
        bookMeta = LecternUtilities.getBookMeta(book);

        if (bookMeta.getPageCount() != 15) {
            throw new NotEnoughPages("You have " + bookMeta.getPageCount() + " pages in your book!!! You need exactly 15!!!");
        }
    }

    public void writeLastMessage(Lectern lectern, Mqtt5Publish mqtt5Publish, Integer powerLevel) {
        String page = ChatColor.DARK_GREEN + "" + ChatColor.BOLD
                + "Last Message: "
                + ChatColor.DARK_RED + "" + ChatColor.BOLD
                + (powerLevel + 1)
                + "\n"
                + ChatColor.DARK_GREEN + "" + ChatColor.BOLD
                + "From Channel: "
                + ChatColor.DARK_RED + "" + ChatColor.BOLD
                + mqtt5Publish.getTopic().toString();

        updateMainPage(lectern, page);
    }

    public void updateMainPage(Lectern lectern, String page) {
        LecternInventory inventory = (LecternInventory) lectern.getSnapshotInventory();

        ItemStack book;
        BookMeta bookMeta;

        try {
            book = LecternUtilities.getItem(inventory);
            bookMeta = LecternUtilities.getBookMeta(book);
        } catch (InvalidLectern | InvalidBook exception) {
            Logger.warning("LecternTracker: " + exception.getMessage());
            return;
        }

        bookMeta.setPage(1, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                + LecternTrackers.getIdentifier()
                + "\n"
                + page);

        book.setItemMeta(bookMeta);
        inventory.setItem(0, book);
    }

    @Override
    public void cleanup() {
        /*
         * Disconnect MQTT Client Properly
         *
         * (that way the MQTT Last Will and Testament isn't executed
         * and can be replaced by a server shutdown message
         * or a tracker destroyed message)
         */

        String message = "Cleanup Called!!!";
        byte[] payload = message.getBytes();

        // Send Message To Topic With UUID
        client.sendMessage(getTopic_uuid(), payload, MqttQos.EXACTLY_ONCE);

        // Send Message To Topic With IGM - Topic may be null here
        if (getTopic_ign() != null) {
            client.sendMessage(getTopic_ign(), payload, MqttQos.EXACTLY_ONCE);
        }

        client.disconnect();

        // Don't handle MySQL Client here as one instance is shared across all trackers!!!
    }
}
