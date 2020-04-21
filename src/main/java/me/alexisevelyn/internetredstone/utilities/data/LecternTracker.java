package me.alexisevelyn.internetredstone.utilities.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.alexisevelyn.internetredstone.database.mysql.MySQLClient;
import me.alexisevelyn.internetredstone.network.mqtt.MQTTClient;
import org.apache.commons.lang.StringUtils;
import org.hashids.Hashids;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class LecternTracker extends Tracker {
    String lecternID;
    MQTTClient mqttClient;
    MySQLClient mySQLClient;

    // Hash IDS
    Hashids hashids;

    // TODO: Better Organize This
    public String getLecternID() throws SQLException {
        ResultSet lecternData = mySQLClient.retrieveLecternDataIfExists(getLocation());

        if (lecternData != null && lecternData.next()) {
            lecternID = lecternData.getString("lecternID");
            setLastKnownPower(lecternData.getInt("lastKnownRedstoneSignal"));
        }

        // Checks if String Was Previously Set Because Of MySQL Data
        // Attempt to retrieve id from database, or if failed, then generate by other means
        if (StringUtils.isBlank(lecternID)) {
            // Temporary Means of Generating Semi-Unique IDs for Testing
            int tries = getMain().getConfiguration().getConfig().getInt("lectern.generate-short-id-tries", 3);
            int maxShortID = getMain().getConfiguration().getConfig().getInt("lectern.max-short-id", 10000);
            boolean success = false;

            SecureRandom random = new SecureRandom();
            int chosenID;
            for (int x = 0; x <= tries; x++) {
                // Generate a Random Integer For Checking Against Database
                chosenID = random.nextInt(maxShortID);

                // If random integer is not being used, use it, otherwise try again
                if (!mySQLClient.isLecternIDUsed(String.valueOf(chosenID))) {
                    lecternID = hashids.encode(chosenID);
                    success = true;

                    break;
                }
            }

            // If failed to find a short and sweet id for the lectern,
            // then just generate a Universally Unique Identifier
            if (!success) {
                lecternID = UUID.randomUUID().toString();
            }
        }

        return lecternID;
    }

    public void setClient(MQTTClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public MQTTClient getClient() {
        return mqttClient;
    }

    public void setHashids(Hashids hashids) {
        this.hashids = hashids;
    }
}
