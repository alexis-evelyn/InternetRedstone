package me.alexisevelyn.internetredstone.utilities;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import org.bukkit.Location;

import java.util.UUID;

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

    public Integer getLastKnownPower() {
        return lastKnownSignal;
    }

    public void setLastKnownPower(Integer lastKnownSignal) {
        this.lastKnownSignal = lastKnownSignal;
    }
}
