package me.alexisevelyn.internetredstone;

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.util.ByteArray;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MQTTClient {
    // Main Class Which Extends JavaPlugin. Needed To Perform Stuff On Main Thread Such As Writing To Log
    Main main;

    // The idea is to store the player's mqtt settings (pointer to pre-setup client) and be able to match it with the location of the block being referenced.
    ConcurrentHashMap<Location, Mqtt5AsyncClient> clients = new ConcurrentHashMap<Location, Mqtt5AsyncClient>();

    public MQTTClient(Main main) {
        this.main = main;

        // For testing in early stage, we register client here.
        // We'll want to register client once a sign or lectern is placed down with the plugin's expected data
        // TODO: Move plugin back to loading as early as possible after proper config and registry is written.
        // So, in plugin.yml set to load to STARTUP instead of POSTWORLD

//        UUID alexis_evelyn = UUID.fromString("f3b4e8a4-7f52-4b0a-a18d-1af64935a89f"); // My UUID For Testing
//        World overworld = Bukkit.getServer().getWorld("world");
//
//        if (overworld == null) {
//            this.main.getLogger().severe(ChatColor.GOLD + "" + ChatColor.BOLD + "Failed To Find World: "
//                    + ChatColor.DARK_RED + "" + ChatColor.BOLD + "world");
//            return;
//        }
//
//        Location spruce_sign = new Location(overworld, 5, 70, 132);
//        registerClient(alexis_evelyn, spruce_sign);
    }

    public Mqtt5AsyncClient registerClient(UUID player_uuid, Location block_location) {
//        MqttUtf8StringImpl username = MqttUtf8StringImpl.of("test");
//
//        byte[] passwordBytes = "redacted".getBytes();
//        ByteBuffer password = ByteBuffer.wrap(passwordBytes);
//
//        final MqttSimpleAuth auth = new MqttSimpleAuth(username, password);
//
//        final Mqtt5AsyncClient client = Mqtt5Client.builder()
//                .serverHost("10.0.0.64")
//                .serverPort(8883)
//                .sslWithDefaultConfig()
//                .simpleAuth(auth)
//                .buildAsync();

        // TODO: Add Player Configuration Here
        // Will be retrieved by database (MySQL) and database will be set by commands from player
        // If none set, use default config

        Mqtt5AsyncClient client = Mqtt5Client.builder().serverHost("broker.hivemq.com").buildAsync();

        // TODO: Check if full and increase size if so - should default to size 16 if not specified
        clients.putIfAbsent(block_location, client);

        return client;
    }

    public Mqtt5AsyncClient getClient(Location block_location) throws MissingResourceException {
        if (!clients.contains(block_location))
            throw new MissingResourceException("MQTT Client Not Registered With Clients HashMap!!!", "MQTTClient", block_location.toString());

        return clients.get(block_location);
    }

    public void sendMessage(Mqtt5AsyncClient client, UUID player_uuid, byte[] payload) throws InterruptedException {
        // Work to keep connection always open and only shutdown on plugin unload

        client.connect()
                .thenAccept(connAck -> this.main.getLogger().info("Player " + player_uuid.toString() + "'s Client connAck: " + connAck))
                .thenCompose(v -> client.publishWith().topic("cmnd/redstone/POWER").payload(payload).qos(MqttQos.EXACTLY_ONCE).send())
                .thenAccept(publishResult -> System.out.println("published " + publishResult))
                .thenCompose(v -> client.disconnect())
                .thenAccept(v -> System.out.println("disconnected"));

        this.main.getLogger().info(ChatColor.GOLD + "" + ChatColor.BOLD + "Running Async MQTT Client!!!");
    }
}
