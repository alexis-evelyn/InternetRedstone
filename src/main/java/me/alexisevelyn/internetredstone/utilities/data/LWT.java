package me.alexisevelyn.internetredstone.utilities.data;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class LWT {
    MqttTopicImpl topic;
    ByteBuffer payload;

    public LWT(MqttTopicImpl topic, ByteBuffer payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public LWT(String topic, String payload) {
        this.topic = MqttTopicImpl.of(topic);
        this.payload = ByteBuffer.wrap(payload.getBytes());
    }

    public MqttWillPublish getWill() {
        return generateLWT(topic, payload);
    }

    private MqttWillPublish generateLWT(MqttTopicImpl topic, ByteBuffer payload) {
        MqttUtf8StringImpl contentType = MqttUtf8StringImpl.of("text/plain");

        return new MqttWillPublish(topic, payload, MqttQos.EXACTLY_ONCE,
                true, MqttPublish.NO_MESSAGE_EXPIRY,
                Mqtt5PayloadFormatIndicator.UTF_8,
                contentType, topic,
                null, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                -1);
    }
}
