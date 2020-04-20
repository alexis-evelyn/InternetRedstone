package me.alexisevelyn.internetredstone.utilities.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class LecternTracker extends Tracker {
    String lecternID;
    MQTTClient client;
}
