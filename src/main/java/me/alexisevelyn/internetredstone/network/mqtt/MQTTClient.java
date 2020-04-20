package me.alexisevelyn.internetredstone.network.mqtt;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import lombok.Data;
import me.alexisevelyn.internetredstone.utilities.data.MQTTSettings;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Data
public class MQTTClient {
    final Mqtt5AsyncClient client;
    final CompletableFuture<Mqtt5ConnAck> connection;

    private final MQTTSettings mqttSettings;

    public MQTTClient(MQTTSettings mqttSettings) {
        this.mqttSettings = mqttSettings;

        MqttSimpleAuth simpleAuth = new MqttSimpleAuth(mqttSettings.getUsername(), mqttSettings.getPassword());

        if (mqttSettings.getTls()) {
            client = Mqtt5Client
                    .builder()
                    .serverHost(mqttSettings.getBroker())
                    .serverPort(mqttSettings.getPort())
                    .sslWithDefaultConfig()
                    .simpleAuth(simpleAuth)
                    .willPublish(mqttSettings.getLWT())
                    .buildAsync();
        } else {
            client = Mqtt5Client
                    .builder()
                    .serverHost(mqttSettings.getBroker())
                    .serverPort(mqttSettings.getPort())
                    .simpleAuth(simpleAuth)
                    .willPublish(mqttSettings.getLWT())
                    .buildAsync();
        }

        connection = client.connect();
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> disconnect() {
        return client.disconnect();
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Mqtt5PublishResult> sendMessage(MqttTopicImpl topic, byte[] payload) {
        return connection.thenCompose(result -> client.publishWith()
                .topic(topic)
                .payload(payload)
                .qos(mqttSettings.getQos())
                .retain(mqttSettings.getRetainMessage())
                .send());
    }

    // Simple method to subscribe to topics with default settings
    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Mqtt5SubAck> subscribe(ArrayList<MqttTopicImpl> topic_strings, Consumer<Mqtt5Publish> callback) {
        ArrayList<MqttSubscription> subscriptionsList = new ArrayList<>();
        for (MqttTopicImpl topic_string : topic_strings) {
            subscriptionsList.add(new MqttSubscription(topic_string.filter(), mqttSettings.getQos(), mqttSettings.getNoLocal(), mqttSettings.getRetainHandling(), mqttSettings.getRetainMessage()));
        }

        ImmutableList<MqttSubscription> subscriptions = ImmutableList.copyOf(subscriptionsList); // A List of Subscriptions To Subscribe To
        MqttSubscribe subscription = new MqttSubscribe(subscriptions, mqttSettings.getProperties()); // MQTT Subscribe Class - Just Put's Data Together

        return client.subscribe(subscription, callback);
    }
}
