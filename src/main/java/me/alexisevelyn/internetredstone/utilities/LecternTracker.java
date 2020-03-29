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
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LecternTracker {
    // Used to track the location of the lectern. Includes the world object too!
    Location location;
    // Used to track the player who owns the lectern. Will aid in allowing using that player's settings.
    UUID player;
    // Used to prevent duplicate signals from being sent
    Integer lastKnownSignal = -1;

    MQTTClient client;
    CompletableFuture<Mqtt5ConnAck> connection;

    public LecternTracker(Location location, UUID player) {
        this.location = location;
        this.player = player;

        // This string will be read from Player's Config Or If None Provided, Server's Config
        String broker = "broker.hivemq.com";

        try {
            client = new MQTTClient(player, broker);
            connection = client.connect();

            // Untested
            connection.thenAccept(read -> subscribe("alexis/redstone/lectern", callback));
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
        String topic = "alexis/redstone/lectern";
        byte[] payload = signal.toString().getBytes();

        client.sendMessage(topic, payload, MqttQos.EXACTLY_ONCE);
    }

    // Choose your method of registry. For Multiple Topics, I Recommend The ImmutableList Method
    // For A Single Topic, I Recommend The String Method
    // For Full Control, Use The MqttSubscribe Method
    public void subscribe(ImmutableList<MqttSubscription> subscriptions, Consumer<Mqtt5Publish> callback) {
        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.
        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties); // MQTT Subscribe Class - Just Put's Data Together

        subscribe(subscription, callback);
    }

    public void subscribe(String topic_string, Consumer<Mqtt5Publish> callback) {
        MqttTopicFilterImpl topic = MqttTopicFilterImpl.of(topic_string); // The Topic To Subscribe To
        MqttQos qos = MqttQos.AT_MOST_ONCE; // How Hard The Server Should Try To Send Us A Message
        boolean noLocal = true; // Don't Send Us A Copy of Messages We Send - Very Important To Prevent Feedback Loop Due To Sharing Input/Output In Same Lectern

        // Docs For Mqtt5RetainHandling - https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104
        Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND; // Send Retained Messages on Subscribe/Don't Send/Only Send If Not Subscribed To This Topic Before (According To Cache)
        boolean retainAsPublished = true; // True means retain flag is set when sent to us if it was set by the publisher.

        MqttSubscription redstone = new MqttSubscription(topic, qos, noLocal, retainHandling, retainAsPublished); // A Subscription To Subscribe To
        ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(redstone); // A List of Subscriptions To Subscribe To

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054
        // https://blog.codecentric.de/en/2017/11/hello-mqtt-version-5-0/
        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.

        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties); // MQTT Subscribe Class - Just Put's Data Together

        subscribe(subscription, callback);
    }

    public void subscribe(MqttTopicFilterImpl topic, Consumer<Mqtt5Publish> callback) {
        MqttQos qos = MqttQos.AT_MOST_ONCE; // How Hard The Server Should Try To Send Us A Message
        boolean noLocal = true; // Don't Send Us A Copy of Messages We Send - Very Important To Prevent Feedback Loop Due To Sharing Input/Output In Same Lectern

        // Docs For Mqtt5RetainHandling - https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104
        Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND; // Send Retained Messages on Subscribe/Don't Send/Only Send If Not Subscribed To This Topic Before (According To Cache)
        boolean retainAsPublished = true; // True means retain flag is set when sent to us if it was set by the publisher.

        MqttSubscription redstone = new MqttSubscription(topic, qos, noLocal, retainHandling, retainAsPublished); // A Subscription To Subscribe To
        ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(redstone); // A List of Subscriptions To Subscribe To

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054
        // https://blog.codecentric.de/en/2017/11/hello-mqtt-version-5-0/
        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.

        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties); // MQTT Subscribe Class - Just Put's Data Together

        subscribe(subscription, callback);
    }

    public void subscribe(MqttSubscribe subscription, Consumer<Mqtt5Publish> callback) {
        client.subscribe(subscription, callback); // Subscribe To Topics And Send Results To Callback Asynchronously
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

    public Location getLocation() {
        return location;
    }

    public MQTTClient getClient() {
        return client;
    }

    public CompletableFuture<Mqtt5ConnAck> getConnection() {
        return connection;
    }

    // Function To Run Asynchronously When Message is Received
    Consumer<Mqtt5Publish> callback = mqtt5Publish -> {
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
            powerLevel = Integer.valueOf(decoded);
        } catch (NumberFormatException exception) {
//            Logger.info(ChatColor.GOLD + "Not A Valid Integer: "
//                    + ChatColor.DARK_PURPLE + decoded);
            return;
        }

        if (0 <= powerLevel && powerLevel <= 15) {
            Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD + "Setting Redstone Signal To (Not Implemented): "
                    + ChatColor.AQUA + "" + ChatColor.BOLD + powerLevel);

            // Doesn't Work Asynchronously
            // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
//            BlockState snapshot = location.getBlock().getState();
//
//            if (snapshot instanceof Lectern) {
//                Lectern lectern = (Lectern) snapshot;
//
//                // TODO: Check to ensure at least 15 pages are in book!!!
//                // And that there is a book.
//                lectern.setPage(powerLevel);
//            }
        }

    };
}
