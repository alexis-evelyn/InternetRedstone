package me.alexisevelyn.internetredstone.utilities.data;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

@Data
public class MQTTSettings {
    String broker;
    Boolean retainMessage;
    MqttQos qos = MqttQos.EXACTLY_ONCE;

    private MqttUtf8StringImpl username;
    private ByteBuffer password = null;

    Boolean tls;
    Integer port;

    private ArrayList<MqttTopicImpl> topics;

    private LastWillAndTestamentBuilder lwt;

    // Docs For Mqtt5RetainHandling - https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104

    private boolean noLocal = true; // Don't Send Us A Copy of Messages We Send - Very Important To Prevent Feedback Loop Due To Sharing Input/Output In Same Lectern
    Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.DO_NOT_SEND; // Send Retained Messages on Subscribe/Don't Send/Only Send If Not Subscribed To This Topic Before (According To Cache)
    boolean retainAsPublished = true; // True means retain flag is set when sent to us if it was set by the publisher.
    MqttUserPropertiesImpl properties = MqttUserPropertiesImpl.NO_USER_PROPERTIES; // User properties are client defined custom pieces of data. They will be forwarded to the receivers of any messages.

    public void addTopic(String topic) {
        topics.add(MqttTopicImpl.of(topic));
    }

    public void removeTopic(String topic) {
        topics.remove(MqttTopicImpl.of(topic));
    }

    public void setLWT(String topic, String payload) {
        lwt = new LastWillAndTestamentBuilder(MqttTopicImpl.of(topic),
                ByteBuffer.wrap(payload.getBytes()));
    }

    public MqttWillPublish getLWT() {
        return lwt.getWill();
    }

    public boolean getNoLocal() {
        return noLocal;
    }

    /* Notes
     *
     * https://www.hivemq.com/blog/mqtt-security-fundamentals-authentication-username-password/
     *
     * The MQTT specification states that you can send a username without password,
     * but it is not possible to send a password without username.
     * MQTT version 3.1.1 also removes the previous recommendation for 12 character passwords.
     */
    public void setSimpleAuth(@Nullable String username, @Nullable String password) {
        if (StringUtils.isNotBlank(username)) {
            this.username = MqttUtf8StringImpl.of(username);

            if (StringUtils.isNotBlank(password))
                this.password = ByteBuffer.wrap(password.getBytes());
        }
    }
}
