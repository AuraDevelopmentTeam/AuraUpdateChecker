package dev.aura.auraupdatechecker.checker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.spongepowered.api.plugin.PluginContainer;

import dev.aura.auraupdatechecker.AuraUpdateChecker;
import dev.aura.auraupdatechecker.util.PluginContainerUtil;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OreAPI {
    public static final String API_URL = "https://ore.spongepowered.org/api/";
    public static final String PROJECT_CALL = "projects/<pluginId>";
    public static final int DEFAULT_TIMEOUT = 250;

    public static boolean isOnOre(PluginContainer plugin) {
        try {
            @Cleanup("disconnect")
            HttpsURLConnection connection = getConnectionForCall(PROJECT_CALL, plugin);
            applyDefaultSettings(connection);
            connection.connect();

            return connection.getResponseCode() == 200;
        } catch (ClassCastException | IOException e) {
            AuraUpdateChecker.getLogger().warn("Could not contact the Ore Repository API for plugin "
                    + PluginContainerUtil.getPluginString(plugin), e);

            return false;
        }
    }

    private static HttpsURLConnection getConnectionForCall(String call, PluginContainer plugin)
            throws MalformedURLException, IOException {
        String url = API_URL + PluginContainerUtil.replacePluginPlaceHolders(call, plugin);

        AuraUpdateChecker.getLogger().trace("Contacting URL: " + url);

        return (HttpsURLConnection) new URL(url).openConnection();
    }

    private static void applyDefaultSettings(HttpsURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(DEFAULT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_TIMEOUT);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
    }
}
