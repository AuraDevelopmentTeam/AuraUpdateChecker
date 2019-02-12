package dev.aura.updatechecker.checker;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.util.PluginContainerUtil;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.plugin.PluginContainer;

@UtilityClass
public class OreAPI {
  public static final String API_URL = "https://ore.spongepowered.org/api/";
  public static final String PROJECT_CALL = "projects/<pluginId>";
  public static final int DEFAULT_TIMEOUT = 250;

  private static AtomicInteger errorCounter = new AtomicInteger(0);

  public static int getErrorCounter() {
    return errorCounter.get();
  }

  public static void resetErrorCounter() {
    errorCounter.set(0);
  }

  public static boolean isOnOre(PluginContainer plugin) {
    try {
      @Cleanup("disconnect")
      HttpsURLConnection connection = getConnectionForCall(PROJECT_CALL, plugin);
      applyDefaultSettings(connection);
      connection.connect();

      return connection.getResponseCode() == 200;
    } catch (ClassCastException | IOException | URISyntaxException e) {
      if (errorCounter.incrementAndGet() == 1) {
        AuraUpdateChecker.getLogger()
            .warn(
                "Could not contact the Ore Repository API for plugin "
                    + PluginContainerUtil.getPluginString(plugin),
                e);
      } else {
        AuraUpdateChecker.getLogger()
            .warn(
                "Could not contact the Ore Repository API for plugin "
                    + PluginContainerUtil.getPluginString(plugin)
                    + ": "
                    + e.getClass().getName());
      }

      return false;
    }
  }

  private static HttpsURLConnection getConnectionForCall(String call, PluginContainer plugin)
      throws ClassCastException, IOException, URISyntaxException {
    String urlStr = API_URL + PluginContainerUtil.replacePluginPlaceHolders(call, plugin);

    AuraUpdateChecker.getLogger().trace("Contacting URL: " + urlStr);

    URL url = new URL(urlStr);
    // Verify URL
    url.toURI();

    return (HttpsURLConnection) url.openConnection();
  }

  private static void applyDefaultSettings(HttpsURLConnection connection) throws ProtocolException {
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(DEFAULT_TIMEOUT);
    connection.setReadTimeout(DEFAULT_TIMEOUT);
    connection.setUseCaches(false);
    connection.setInstanceFollowRedirects(true);
  }
}
