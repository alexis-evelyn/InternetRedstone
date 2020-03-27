package me.alexisevelyn.internetredstone.network.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.*;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import me.alexisevelyn.internetredstone.utilities.Logger;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MQTTClient {
    Mqtt5AsyncClient client;
    CompletableFuture<Mqtt5ConnAck> connection;

    public MQTTClient(UUID player_uuid, String broker) {
        writeInfo(ChatColor.GOLD + "Registered Client For: " + player_uuid);
        writeInfo(ChatColor.GOLD + "With Broker: " + broker);
        client = Mqtt5Client.builder().serverHost(broker).buildAsync();
    }

    public CompletableFuture<Mqtt5ConnAck> connect() {
        connection = client.connect();
        return connection;
    }

    public CompletableFuture<Mqtt5PublishResult> sendMessage(String topic, byte[] payload, MqttQos qos) {
        return connection.thenCompose(result -> client.publishWith()
                .topic(topic)
                .payload(payload)
                .qos(qos)
                .send());
    }

    public CompletableFuture<Void> readResult(CompletableFuture<Mqtt5PublishResult> result) {
        // Not Sure How To Implement Yet
        return result.thenAccept(read -> writeInfo(ChatColor.DARK_GREEN + "Result: " + read));
    }

    public CompletableFuture<Void> disconnect() {
        return client.disconnect();
    }

    private void writeInfo(String message) {
        Logger.info(message);
    }
}
