package me.alexisevelyn.internetredstone.network.mqtt;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MQTTClient {
    final Mqtt5AsyncClient client;
    CompletableFuture<Mqtt5ConnAck> connection;

    String broker;

    public MQTTClient(UUID player_uuid, String broker) {
        writeInfo(ChatColor.GOLD + "Registered Client For: "
                + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + player_uuid);

        writeInfo(ChatColor.GOLD + "With Broker: "
                + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + broker);

        this.broker = broker;
        client = Mqtt5Client.builder().serverHost(broker).buildAsync();
    }

    public String getBroker() {
        return broker;
    }

    public CompletableFuture<Mqtt5ConnAck> getConnection() {
        return connection;
    }

    public CompletableFuture<Mqtt5ConnAck> connect() {
        connection = client.connect();
        return connection;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> disconnect() {
        return client.disconnect();
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Mqtt5PublishResult> sendMessage(String topic, byte[] payload, MqttQos qos) {
        return connection.thenCompose(result -> client.publishWith()
                .topic(topic)
                .payload(payload)
                .qos(qos)
                .send());
    }

//    @SuppressWarnings("UnusedReturnValue")
//    public CompletableFuture<Void> readResult(CompletableFuture<Mqtt5PublishResult> result) {
//        // Not Sure How To Implement Yet
//        return result.thenAccept(read -> writeInfo(ChatColor.DARK_GREEN + "Result: " + read));
//    }

    private void writeInfo(String message) {
        Logger.info(message);
    }

    // Simple method to subscribe to topics with default settings
    public void subscribe(ArrayList<String> topic_strings, Consumer<Mqtt5Publish> callback) {
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

        MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.
        MqttSubscribe subscription = new MqttSubscribe(subscriptions, properties); // MQTT Subscribe Class - Just Put's Data Together

        // I'm suppressing the single line lambda warning as I'm going to eventually add in more logger lines
        // TODO: Add more logger lines to help with debugging in the future. Also figure out how to determine if connected or not!!!
        //noinspection CodeBlock2Expr
        subscribe(subscription, callback).thenAcceptAsync(mqtt5SubAck -> {
            Logger.finer("Subscribed Reason (Debug): "
                    + mqtt5SubAck.getReasonString());
        });
    }

    // Ultimately Called Subscribe Function
    // When extending the class, use this to have full control over MQTT Setup
    public CompletableFuture<Mqtt5SubAck> subscribe(MqttSubscribe subscription, Consumer<Mqtt5Publish> callback) {
        // For Super Debug - List All Subscribed Topics Every Time Ready to Subscribe
//        if (Logger.isDebugMode())
//            for (MqttSubscription line : subscription.getSubscriptions().trim()) // .trim() seems to strip everything, find a way to get an iterable list
//                Logger.finer(ChatColor.DARK_GREEN + "" + ChatColor.BOLD
//                        + "Topics To Subscribe: "
//                        + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
//                        + line.getTopicFilter().toString());

        return client.subscribe(subscription, callback);
    }
}
