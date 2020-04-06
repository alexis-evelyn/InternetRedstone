package me.alexisevelyn.internetredstone.utilities;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import me.alexisevelyn.internetredstone.Main;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidBook;
import me.alexisevelyn.internetredstone.utilities.exceptions.InvalidLectern;
import me.alexisevelyn.internetredstone.utilities.exceptions.NotEnoughPages;
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
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LecternTracker {
    // Main class used to get synchronous execution (Don't Mark as Final Here)
    @SuppressWarnings("CanBeFinal")
    Main main;

    // Used to track the location of the lectern. Includes the world object too!
    final Location location;

    // Used to track the player who owns the lectern. Will aid in allowing using that player's settings.
    @SuppressWarnings("unused")
    final UUID player;

    // Used to prevent duplicate signals from being sent
    Integer lastKnownSignal = -1;

    MQTTClient client;
    CompletableFuture<Mqtt5ConnAck> connection;

    String topic_uuid;
    String topic_ign;

    MySQLClient mySQLClient;
    String broker;
    Integer temporary_random_number;

    public LecternTracker(@NotNull Main main, Location location, UUID player) {
        this.location = location;
        this.player = player;
        this.main = main;

        this.mySQLClient = main.getMySQLClient();

        // This string will be read from Player's Config Or If None Provided, Server's Config
        broker = "broker.hivemq.com";

        // Temporarily Hardcoded Server Value for Testing
        String server_name = "alexis";

        // Temporary Means of Generating Semi-Unique IDs for Testing
        Random temporary = new Random();
        temporary_random_number = temporary.nextInt(100);

        try {
            client = new MQTTClient(player, broker);
            connection = client.connect();

            // List of Topics to Subscribe To
            topic_uuid = server_name + "/" + player + "/" + temporary_random_number; // Topic based on player's uuid

            // ArrayList of Topics to Subscribe To
            ArrayList<String> topics = new ArrayList<>();
            topics.add(topic_uuid);

            // Get Player's Name if Possible, Otherwise, Just Stick With UUID
            Player online_player = Bukkit.getPlayer(player);
            if (online_player != null) {
                topic_ign = server_name + "/" + online_player.getName() + "/" + temporary_random_number; // Topic based on player's ign
                topics.add(topic_ign);
            }

            // Subscribe to Topics through ArrayList method
            // For those wondering, why use thenAccept, it's to force the subscription to wait until a connection has been established.
            connection.thenAccept(read -> subscribe(topics, callback)).thenAccept(mysql -> addDatabaseEntry());
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
        mySQLClient.storeUserPreferences(broker, null, null, player, null, getLastKnownPower());
        mySQLClient.registerLectern(player, location, String.valueOf(temporary_random_number), lastKnownSignal);
    }

    // Choose your method of registry. For Multiple Topics, I Recommend The ImmutableList Method
    // For A Single Topic, I Recommend The String Method
    // For Full Control, Use The MqttSubscribe Method
    private void subscribe(ArrayList<String> topic_strings, Consumer<Mqtt5Publish> callback) {
        // This is designed to keep the Constructor Clean
        MqttQos qos = MqttQos.AT_MOST_ONCE; // How Hard The Server Should Try To Send Us A Message
        boolean noLocal = true; // Don't Send Us A Copy of Messages We Send - Very Important To Prevent Feedback Loop Due To Sharing Input/Output In Same Lectern

        // Docs For Mqtt5RetainHandling - https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104
        Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND; // Send Retained Messages on Subscribe/Don't Send/Only Send If Not Subscribed To This Topic Before (According To Cache)
        boolean retainAsPublished = true; // True means retain flag is set when sent to us if it was set by the publisher.

        ArrayList<MqttSubscription> subscriptionsList = new ArrayList<>();
        for (String topic_string : topic_strings) {
            Logger.finer(ChatColor.GOLD + "" + ChatColor.BOLD
                    + "Topics to Subscribe: "
                    + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                    + topic_string);

            //noinspection ConstantConditions
            subscriptionsList.add(new MqttSubscription(MqttTopicFilterImpl.of(topic_string), qos, noLocal, retainHandling, retainAsPublished));
        }

        ImmutableList<MqttSubscription> subscriptions = ImmutableList.copyOf(subscriptionsList); // A List of Subscriptions To Subscribe To

        subscribe(subscriptions, callback);
    }

    // Passing In Multiple Subscriptions (Topics and Settings) In One Subscription
    public void subscribe(ImmutableList<MqttSubscription> subscriptions, Consumer<Mqtt5Publish> callback) {
        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.
        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties); // MQTT Subscribe Class - Just Put's Data Together

        subscribe(subscription, callback);
    }

    // Passing In A Single Topic String
    @SuppressWarnings("unused")
    public void subscribe(String topic_string, Consumer<Mqtt5Publish> callback) {
        MqttTopicFilterImpl topic = MqttTopicFilterImpl.of(topic_string); // The Topic To Subscribe To

        subscribe(topic, callback);
    }

    // Passing In A Single MqttTopicFilterImpl Directly
    public void subscribe(MqttTopicFilterImpl topic, Consumer<Mqtt5Publish> callback) {
        MqttQos qos = MqttQos.AT_MOST_ONCE; // How Hard The Server Should Try To Send Us A Message
        boolean noLocal = true; // Don't Send Us A Copy of Messages We Send - Very Important To Prevent Feedback Loop Due To Sharing Input/Output In Same Lectern

        // Docs For Mqtt5RetainHandling - https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104
        Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND; // Send Retained Messages on Subscribe/Don't Send/Only Send If Not Subscribed To This Topic Before (According To Cache)
        boolean retainAsPublished = true; // True means retain flag is set when sent to us if it was set by the publisher.

        @SuppressWarnings("ConstantConditions")
        MqttSubscription redstone = new MqttSubscription(topic, qos, noLocal, retainHandling, retainAsPublished); // A Subscription To Subscribe To
        ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(redstone); // A List of Subscriptions To Subscribe To

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054
        // https://blog.codecentric.de/en/2017/11/hello-mqtt-version-5-0/
        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.

        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties); // MQTT Subscribe Class - Just Put's Data Together

        subscribe(subscription, callback);
    }

    // Ultimately Called Subscribe Function
    public void subscribe(MqttSubscribe subscription, Consumer<Mqtt5Publish> callback) {
        CompletableFuture<Mqtt5SubAck> subscribed = client.subscribe(subscription, callback); // Subscribe To Topics And Send Results To Callback Asynchronously

        // I'm suppressing the single line lambda warning as I'm going to eventually add in more logger lines
        // TODO: Add more logger lines to help with debugging in the future. Also figure out how to determine if connected or not!!!
        //noinspection CodeBlock2Expr
        subscribed.thenAcceptAsync(mqtt5SubAck -> {
            Logger.finer("Subscribed Reason (Debug): "
                    + mqtt5SubAck.getReasonString());
        });
    }

    public void cleanup() {
        client.disconnect();
    }

    public boolean isLastKnownPower(Integer currentPower) {
        return currentPower.equals(getLastKnownPower());
    }

    public Integer getLastKnownPower() {
        return lastKnownSignal;
    }

    public void setLastKnownPower(Integer lastKnownSignal) {
        this.lastKnownSignal = lastKnownSignal;
    }

    @SuppressWarnings("unused")
    public Location getLocation() {
        return location;
    }

    @SuppressWarnings("unused")
    public MQTTClient getClient() {
        return client;
    }

    @SuppressWarnings("unused")
    public CompletableFuture<Mqtt5ConnAck> getConnection() {
        return connection;
    }

    public String getTopic_uuid() {
        return topic_uuid;
    }

    @SuppressWarnings("unused")
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
            Bukkit.getScheduler().runTask(main, new Runnable() {
                @Override
                public void run() {
                    Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD + "Setting Redstone Signal To: "
                            + ChatColor.AQUA + "" + ChatColor.BOLD + (powerLevel + 1));

                    try {
                        BlockState snapshot = location.getBlock().getState();

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
}
