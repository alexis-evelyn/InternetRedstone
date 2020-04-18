package me.alexisevelyn.internetredstone.network.mqtt;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
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
import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.Logger;
import me.alexisevelyn.internetredstone.utilities.data.LastWillAndTestamentBuilder;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Data
public class MQTTClient {
    final Mqtt5AsyncClient client;
    final CompletableFuture<Mqtt5ConnAck> connection;

    @SuppressWarnings("unused")
    public MQTTClient(String broker, Integer port, Boolean tls, LastWillAndTestamentBuilder lwt) {
        if (tls) {
            client = Mqtt5Client
                    .builder()
                    .serverHost(broker)
                    .serverPort(port)
                    .sslWithDefaultConfig()
                    .willPublish(lwt.getWill())
                    .buildAsync();
        } else {
            client = Mqtt5Client
                    .builder()
                    .serverHost(broker)
                    .serverPort(port)
                    .willPublish(lwt.getWill())
                    .buildAsync();
        }

        connection = client.connect();
    }

    public MQTTClient(String broker, Integer port, Boolean tls, String username, @Nullable String password, LastWillAndTestamentBuilder lwt) {
        MqttUtf8StringImpl formattedUsername = MqttUtf8StringImpl.of(username);
        ByteBuffer formattedPassword = null;

        if (password != null)
            formattedPassword = ByteBuffer.wrap(password.getBytes());

        MqttSimpleAuth simpleAuth = new MqttSimpleAuth(formattedUsername, formattedPassword);

        if (tls) {
            client = Mqtt5Client
                    .builder()
                    .serverHost(broker)
                    .serverPort(port)
                    .sslWithDefaultConfig()
                    .simpleAuth(simpleAuth)
                    .willPublish(lwt.getWill())
                    .buildAsync();
        } else {
            client = Mqtt5Client
                    .builder()
                    .serverHost(broker)
                    .serverPort(port)
                    .simpleAuth(simpleAuth)
                    .willPublish(lwt.getWill())
                    .buildAsync();
        }

        connection = client.connect();
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> disconnect() {
        return client.disconnect();
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Mqtt5PublishResult> sendMessage(String topic, byte[] payload, MqttQos qos) {
        return sendMessage(topic, payload, qos, false);
    }


    public CompletableFuture<Mqtt5PublishResult> sendMessage(String topic, byte[] payload, MqttQos qos, Boolean retain) {
        return connection.thenCompose(result -> client.publishWith()
                .topic(topic)
                .payload(payload)
                .qos(qos)
                .retain(retain)
                .send());
    }

    // Simple method to subscribe to topics with default settings
    public void subscribe(ArrayList<String> topic_strings, Consumer<Mqtt5Publish> callback) {
        // This is designed to keep the Constructor Clean
        MqttQos qos = MqttQos.AT_MOST_ONCE; // How Hard The Server Should Try To Send Us A Message
        boolean noLocal = true; // Don't Send Us A Copy of Messages We Send - Very Important To Prevent Feedback Loop Due To Sharing Input/Output In Same Lectern

        // Docs For Mqtt5RetainHandling - https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104
        Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.DO_NOT_SEND; // Send Retained Messages on Subscribe/Don't Send/Only Send If Not Subscribed To This Topic Before (According To Cache)
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
        return client.subscribe(subscription, callback);
    }
}
