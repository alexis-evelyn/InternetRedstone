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
public class LastWillAndTestamentBuilder {
    MqttTopicImpl topic;
    MqttQos qos = MqttQos.EXACTLY_ONCE;
    Mqtt5PayloadFormatIndicator payloadFormatIndicator = Mqtt5PayloadFormatIndicator.UTF_8;
    MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
    MqttUtf8StringImpl contentType = MqttUtf8StringImpl.of("text/plain");

    ByteBuffer payload;
    ByteBuffer correlationData = null;

    long messageExpiry = MqttPublish.NO_MESSAGE_EXPIRY;
    boolean retainMessage = true;
    int delayInterval = -1;

    @SuppressWarnings("unused")
    public LastWillAndTestamentBuilder(MqttTopicImpl topic, ByteBuffer payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public LastWillAndTestamentBuilder(String topic, String payload) {
        this.topic = MqttTopicImpl.of(topic);
        this.payload = ByteBuffer.wrap(payload.getBytes());
    }

    public MqttWillPublish getWill() {
        return new MqttWillPublish(topic, payload, qos,
                retainMessage, messageExpiry,
                payloadFormatIndicator,
                contentType, topic,
                correlationData, userProperties,
                delayInterval);
    }
}
