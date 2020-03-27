package me.alexisevelyn.internetredstone.utilities;

import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import org.bukkit.Location;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

public class LecternTracker {
    // Used to track the location of the lectern. Includes the world object too!
    Location location;
    // Used to track the player who owns the lectern. Will aid in allowing using that player's settings.
    UUID player;

    MQTTClient client;

    public LecternTracker(Location location, UUID player) {
        this.location = location;
        this.player = player;

        // This string will be read from Player's Config Or If None Provided, Server's Config
        String broker = "broker.hivemq.com";

        client = new MQTTClient(player, broker);
        client.connect();
    }

    public void sendRedstoneUpdate(Integer signal) {
        String topic = "cmnd/redstone/POWER";
        byte[] payload = signal.toString().getBytes();

        client.sendMessage(topic, payload, MqttQos.EXACTLY_ONCE);
    }

    public void cleanup() {
        client.disconnect();
    }
}
