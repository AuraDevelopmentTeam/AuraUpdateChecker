package dev.aura.updatechecker.checker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.aura.lib.version.Version;
import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.util.PluginContainerUtil;
import dev.aura.updatechecker.util.PluginVersionInfo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

@UtilityClass
public class OreAPI {
  public static final String API_URL = "https://ore.spongepowered.org/api/v1/";
  public static final String PROJECT_CALL = "projects/<pluginId>";
  public static final String VERSION_CALL = PROJECT_CALL + "/versions?limit=10&offset=";

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

      logDebug(
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

  public static Optional<SortedMap<Date, Version>> getAllVersions(PluginContainer plugin) {
    try {
      final SortedMap<Date, Version> allVersions = new TreeMap<>(Comparator.reverseOrder());
      final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

      for (int i = 0; true; i += 10) {
        @Cleanup("disconnect")
        HttpsURLConnection connection = getConnectionForCall(VERSION_CALL + i, plugin);
        connection.connect();

        if (connection.getResponseCode() != 200) {
          return Optional.empty();
        }

        final JsonArray versions =
            gson.fromJson(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8),
                JsonArray.class);

        for (JsonElement version : versions) {
          final Date date =
              dateFormat.parse(version.getAsJsonObject().get("createdAt").getAsString());
          final Version pluginVersion =
              new Version(version.getAsJsonObject().get("name").getAsString());

          allVersions.put(date, pluginVersion);
        }

        if (versions.size() < 10) {
          break;
        }
      }

      logDebug(
          "Available Versions for plugin "
              + PluginContainerUtil.getPluginString(plugin)
              + " are: "
              + allVersions
                  .entrySet()
                  .stream()
                  .map(
                      entry ->
                          dateFormat.format(entry.getKey()) + ": " + entry.getValue().getInput())
                  .collect(Collectors.joining("\n\t", "\n\t", "")));

      return Optional.of(allVersions);
    } catch (ClassCastException | IOException | URISyntaxException | ParseException e) {
      printErrorMessage(plugin, e);

      return Optional.empty();
    }
  }

  public static Optional<PluginVersionInfo> getPluginVersionInfo(PluginContainer plugin) {
    final Optional<Version> recommendedVersion = getRecommendedVersion(plugin);

    if (!recommendedVersion.isPresent()) {
      return Optional.empty();
    }

    final Optional<SortedMap<Date, Version>> allVersions = getAllVersions(plugin);

    if (!allVersions.isPresent()) {
      return Optional.empty();
    }

    return Optional.of(new PluginVersionInfo(plugin, recommendedVersion.get(), allVersions.get()));
  }

  private static HttpsURLConnection getConnectionForCall(String call, PluginContainer plugin)
      throws ClassCastException, IOException, URISyntaxException {
    String urlStr = API_URL + PluginContainerUtil.replacePluginPlaceHolders(call, plugin);

    logTrace("Contacting URL: " + urlStr);

    URL url = new URL(urlStr);
    // Verify URL
    url.toURI();

    final HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
    applyDefaultSettings(httpsConnection);

    return httpsConnection;
  }

  private static void applyDefaultSettings(HttpsURLConnection connection) throws ProtocolException {
    final int defaultTimeout = AuraUpdateChecker.getConfig().getTiming().getConnectionTimeout();

    connection.setRequestMethod("GET");
    connection.setConnectTimeout(defaultTimeout);
    connection.setReadTimeout(defaultTimeout);
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

  private static void logDebug(String message) {
    final Logger logger = AuraUpdateChecker.getLogger();

    if (AuraUpdateChecker.getConfig().getGeneral().getDebug()) {
      logger.info("[Debug]: " + message);
    } else {
      logger.debug(message);
    }
  }

  private static void logTrace(String message) {
    final Logger logger = AuraUpdateChecker.getLogger();

    if (AuraUpdateChecker.getConfig().getGeneral().getDebug()) {
      logger.info("[Trace]: " + message);
    } else {
      logger.trace(message);
    }
  }
}
