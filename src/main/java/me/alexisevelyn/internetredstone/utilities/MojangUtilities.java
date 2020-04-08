package me.alexisevelyn.internetredstone.utilities;

import me.alexisevelyn.internetredstone.utilities.exceptions.SyncThreadNotAllowed;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.text.MessageFormat;
import java.util.UUID;

public class MojangUtilities {
    static Thread sync;
    static String uuid_to_name = "https://api.mojang.com/user/profiles/{0}/names";

    // TODO: I may replace async thread requirement with Callable and pass that back instead of string
    public static String getLatestName(UUID player) throws IOException, SyncThreadNotAllowed {
        if (isSyncThread(Thread.currentThread()))
            throw new SyncThreadNotAllowed("This function uses Http Requests. Call from an asynchronous thread!!!");

        String url = MessageFormat.format(uuid_to_name, player.toString().replace("-", ""));

        URL reference = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) reference.openConnection();



        return null;
    }

    private static String retrieveHttpResponse() {

        return null;
    }

    private static JSONObject parseForJSON() {

        return null;
    }

    private static boolean isSyncThread(Thread unknown) {
        return sync.equals(unknown);
    }

    public static void setSync(Thread sync_thread) {
        sync = sync_thread;
    }
}
