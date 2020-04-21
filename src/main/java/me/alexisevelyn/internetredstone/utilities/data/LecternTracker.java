package me.alexisevelyn.internetredstone.utilities.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;

import javax.annotation.Nullable;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class LecternTracker extends Tracker {
    String lecternID;
    String topic_uuid;
    String topic_ign;
    String broker;
    Boolean retainMessage;
    @Nullable String username;
    @Nullable String password;
    Boolean tls;
    Integer port;

    MQTTClient client;
}
