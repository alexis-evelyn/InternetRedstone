package me.alexisevelyn.internetredstone.utilities.abstracted;

import lombok.Data;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;

@Data
public abstract class LecternTracker extends Tracker {
    String lecternID;
    String topic_uuid;
    String topic_ign;
    String broker;
    Boolean retainMessage;

    MQTTClient client;
}
