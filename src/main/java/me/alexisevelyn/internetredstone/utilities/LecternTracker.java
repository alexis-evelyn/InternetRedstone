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
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.UUID;
import java.util.function.Consumer;

public class LecternTracker {
    // Used to track the location of the lectern. Includes the world object too!
    Location location;
    // Used to track the player who owns the lectern. Will aid in allowing using that player's settings.
    UUID player;
    // Used to prevent duplicate signals from being sent
    Integer lastKnownSignal;

    MQTTClient client;

    public LecternTracker(Location location, UUID player) {
        this.location = location;
        this.player = player;

        // This string will be read from Player's Config Or If None Provided, Server's Config
        String broker = "broker.hivemq.com";

        try {
            client = new MQTTClient(player, broker);
            client.connect();
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
        String topic = "cmnd/redstone/POWER";
        byte[] payload = signal.toString().getBytes();

        client.sendMessage(topic, payload, MqttQos.EXACTLY_ONCE);
    }

    public void subscribe() {
        MqttTopicFilterImpl topic = MqttTopicFilterImpl.of("cmnd/redstone/POWER");
        MqttQos qos = MqttQos.EXACTLY_ONCE;
        boolean noLocal = true;
        Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND; // TODO: Figure out what this does!!!
        boolean retainAsPublished = true; // TODO: Figure out what this does!!!
        
        
        MqttSubscription redstone = new MqttSubscription(topic, qos, noLocal, retainHandling, retainAsPublished);
        ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(redstone);
        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // TODO: Figure out what this does!!!

        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties);
        Consumer<Mqtt5Publish> callback = new Consumer<Mqtt5Publish>() {
            @Override
            public void accept(Mqtt5Publish mqtt5Publish) {
                Logger.info(ChatColor.GOLD + "" + ChatColor.BOLD
                        + "Received Message On Topic: "
                        + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                        + mqtt5Publish.getTopic().toString()
                        + "Message: \n"
                        + ChatColor.AQUA + "" + ChatColor.BOLD
                        + mqtt5Publish.getPayload().toString()
                        + ChatColor.RESET);
            }
        };

        client.subscribe(subscription, callback);
    }

    public void cleanup() {
        client.disconnect();
    }

    public Integer getLastKnownPower() {
        return lastKnownSignal;
    }

    public void setLastKnownPower(Integer lastKnownSignal) {
        this.lastKnownSignal = lastKnownSignal;
    }
}
