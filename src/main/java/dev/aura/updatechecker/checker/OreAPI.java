package dev.aura.updatechecker.checker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.aura.lib.version.Version;
import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.util.PluginContainerUtil;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

@UtilityClass
public class OreAPI {
  public static final String API_URL = "https://ore.spongepowered.org/api/v1/";
  public static final String PROJECT_CALL = "projects/<pluginId>";
  public static final String VERSION_CALL = PROJECT_CALL + "/versions";
  public static final int DEFAULT_TIMEOUT = 250;

  private static final AtomicInteger errorCounter = new AtomicInteger(0);
  private static final Gson gson = new Gson();

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
      connection.setRequestMethod("HEAD"); // We don't care about the content yet
      connection.connect();

      return connection.getResponseCode() == 200;
    } catch (ClassCastException | IOException | URISyntaxException e) {
      printErrorMessage(plugin, e);

      return false;
    }
  }

  public static Optional<Version> getRecommendedVersion(PluginContainer plugin) {
    try {
      @Cleanup("disconnect")
      HttpsURLConnection connection = getConnectionForCall(PROJECT_CALL, plugin);
      connection.connect();

      if (connection.getResponseCode() != 200) {
        return Optional.empty();
      }

      final String recommendedVersion =
          gson.fromJson(
                  new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8),
                  JsonObject.class)
              .get("recommended")
              .getAsJsonObject()
              .get("name")
              .getAsString();

      AuraUpdateChecker.getLogger()
          .debug(
              "Recommended Version for plugin "
                  + PluginContainerUtil.getPluginString(plugin)
                  + " is: "
                  + recommendedVersion);

      return Optional.of(new Version(recommendedVersion));
    } catch (ClassCastException | IOException | URISyntaxException | IllegalStateException e) {
      printErrorMessage(plugin, e);

      return Optional.empty();
    }
  }

  private static HttpsURLConnection getConnectionForCall(String call, PluginContainer plugin)
      throws ClassCastException, IOException, URISyntaxException {
    String urlStr = API_URL + PluginContainerUtil.replacePluginPlaceHolders(call, plugin);

    AuraUpdateChecker.getLogger().trace("Contacting URL: " + urlStr);

    URL url = new URL(urlStr);
    // Verify URL
    url.toURI();

    final HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
    applyDefaultSettings(httpsConnection);

    return httpsConnection;
  }

  private static void applyDefaultSettings(HttpsURLConnection connection) throws ProtocolException {
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(DEFAULT_TIMEOUT);
    connection.setReadTimeout(DEFAULT_TIMEOUT);
    connection.setUseCaches(false);
    connection.setInstanceFollowRedirects(true);
  }

  private static void printErrorMessage(PluginContainer plugin, Throwable e) {
    final Logger logger = AuraUpdateChecker.getLogger();

    if (errorCounter.incrementAndGet() == 1) {
      logger.warn(
          "Could not contact the Ore Repository API for plugin "
              + PluginContainerUtil.getPluginString(plugin),
          e);
    } else {
      logger.warn(
          "Could not contact the Ore Repository API for plugin "
              + PluginContainerUtil.getPluginString(plugin)
              + ": "
              + e.getClass().getName());
    }
  }
}
