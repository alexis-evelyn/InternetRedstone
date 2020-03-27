package me.alexisevelyn.internetredstone;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        MQTTClient mqttClient = new MQTTClient(this);

        getServer().getPluginManager().registerEvents(new EventListener(this, mqttClient), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
