package me.alexisevelyn.internetredstone.utilities.abstracted;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class LecternTracker extends Tracker {
    String lecternID;
    String topic_uuid;
    String topic_ign;
    String broker;
    Boolean retainMessage;

    MQTTClient client;
}
