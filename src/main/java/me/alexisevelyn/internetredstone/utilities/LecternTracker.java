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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class LecternTracker extends Tracker {
    // Main class used to get synchronous execution (Don't Mark as Final Here)
    @SuppressWarnings("CanBeFinal")
    Main main;

    String lecternID;
    String topic_uuid;
    String topic_ign;

    MQTTClient client;
    MySQLClient mySQLClient;

    public LecternTracker(@NotNull Main main, Location location, UUID player) {
        setLocation(location);
        setPlayer(player);

        this.main = main;
        this.mySQLClient = main.getMySQLClient();

        // Temporarily Hardcoded Server Value for Testing
        String server_name = "alexis";

        // Temporary Means of Generating Semi-Unique IDs for Testing
        Random temporary = new Random();
        lecternID = String.valueOf(temporary.nextInt(100));

        try {
            // This string will be read from Player's Config Or If None Provided, Server's Config
            client = new MQTTClient(player, "broker.hivemq.com");

            // List of Topics to Subscribe To
            topic_uuid = server_name + "/" + player + "/" + getLecternID(); // Topic based on player's uuid

            // ArrayList of Topics to Subscribe To
            ArrayList<String> topics = new ArrayList<>();
            topics.add(topic_uuid);

            // Get Player's Name if Possible, Otherwise, Just Stick With UUID
            Player online_player = Bukkit.getPlayer(player);
            if (online_player != null) {
                topic_ign = server_name + "/" + online_player.getName() + "/" + getLecternID(); // Topic based on player's ign
                topics.add(topic_ign);
            }

            // Subscribe to Topics through ArrayList method
            // For those wondering, why use thenAccept, it's to force the subscription to wait until a connection has been established.
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

    public void sendRedstoneUpdate(Integer signal) {
        byte[] payload = signal.toString().getBytes();

        // Send Message To Topic With UUID
        client.sendMessage(getTopic_uuid(), payload, MqttQos.EXACTLY_ONCE);

        // Send Message To Topic With IGM - Topic may be null here
        if (getTopic_ign() != null) {
            client.sendMessage(getTopic_ign(), payload, MqttQos.EXACTLY_ONCE);
        }
    }

    private void addDatabaseEntry() {
        // TODO: Add String for username, password, and ign!!!

        try {
            // Update Number of Registered Lecterns and IGN
            //mySQLClient.storeUserPreferences(client.getBroker(), null, null, getPlayer(), null, 0);

            mySQLClient.registerLectern(getPlayer(), getLocation(), getLecternID(), getLastKnownPower());
        } catch (SQLException exception) {
            Logger.severe("Failed to add database entries in Lectern Tracker!!!");

            Logger.printException(exception);
        }
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

    public String getLecternID() {
        return lecternID;
    }

    public String getTopic_uuid() {
        return topic_uuid;
    }

    public String getTopic_ign() {
        return topic_ign;
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
            Bukkit.getScheduler().runTask(main, () -> {
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
        client.disconnect();
        mySQLClient.disconnect();
    }
}
