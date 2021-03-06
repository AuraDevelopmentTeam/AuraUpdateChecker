package dev.aura.updatechecker.checker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.aura.lib.version.Version;
import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.message.PluginMessages;
import dev.aura.updatechecker.util.PluginContainerUtil;
import dev.aura.updatechecker.util.PluginVersionInfo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.net.ssl.HttpsURLConnection;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

@UtilityClass
public class OreAPI {
  public static final String API_URL = "https://ore.spongepowered.org/api/v2/";
  public static final String AUTHENTICATE_CALL = "authenticate";
  public static final String PROJECT_CALL = "projects/<pluginId>";
  public static final String VERSION_CALL = PROJECT_CALL + "/versions?limit=10&offset=";

  private static final AtomicInteger errorCounter = new AtomicInteger(0);
  private static final Gson gson = new Gson();

  @VisibleForTesting static String authHeader = null;

  public static int getErrorCounter() {
    return errorCounter.get();
  }

  public static void resetErrorCounter() {
    errorCounter.set(0);
  }

  public static boolean authenticate() {
    authHeader = null;

    try {
      @Cleanup("disconnect")
      HttpsURLConnection connection = getConnectionForCall(AUTHENTICATE_CALL);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8")) {
          // 10 minutes to be save
          osw.write("{\"expires_in\":600}");
          osw.flush();
        }
      }

      connection.connect();

      if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) return false;

      authHeader =
          "OreApi session=\""
              + gson.fromJson(
                      new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8),
                      JsonObject.class)
                  .get("session")
                  .getAsString()
              + '"';

      return true;
    } catch (SocketTimeoutException e) {
      printNetworkError(null, e);
    } catch (ClassCastException | IOException | URISyntaxException e) {
      printErrorMessage(null, e);
    }

    return false;
  }

  public static boolean isOnOre(PluginContainer plugin) {
    try {
      @Cleanup("disconnect")
      HttpsURLConnection connection = getConnectionForCall(PROJECT_CALL, plugin);
      connection.setRequestMethod("HEAD"); // We don't care about the content yet
      connection.connect();

      return connection.getResponseCode() == HttpsURLConnection.HTTP_OK;
    } catch (SocketTimeoutException e) {
      printNetworkError(plugin, e);
    } catch (ClassCastException | IOException | URISyntaxException e) {
      printErrorMessage(plugin, e);
    }

    return false;
  }

  public static Optional<Version> getRecommendedVersion(PluginContainer plugin) {
    try {
      @Cleanup("disconnect")
      HttpsURLConnection connection = getConnectionForCall(PROJECT_CALL, plugin);
      connection.connect();

      if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
        return Optional.empty();
      }

      final Optional<Version> recommendedVersion =
          StreamSupport.stream(
                  gson.fromJson(
                          new InputStreamReader(
                              connection.getInputStream(), StandardCharsets.UTF_8),
                          JsonObject.class)
                      .get("promoted_versions")
                      .getAsJsonArray()
                      .spliterator(),
                  false)
              .map(el -> el.getAsJsonObject().get("version").getAsString())
              .map(Version::new)
              .max(Comparator.naturalOrder());

      if (recommendedVersion.isPresent())
        logDebug(
            PluginMessages.LOG_RECOMMENDED_VERSION.getMessageRaw(
                ImmutableMap.of(
                    "plugin",
                    PluginContainerUtil.getPluginString(plugin),
                    "version",
                    recommendedVersion.get().getInput())));

      return recommendedVersion;
    } catch (SocketTimeoutException e) {
      printNetworkError(plugin, e);
    } catch (ClassCastException | IOException | URISyntaxException | IllegalStateException e) {
      printErrorMessage(plugin, e);
    }

    return Optional.empty();
  }

  public static Optional<SortedMap<Instant, Version>> getAllVersions(PluginContainer plugin) {
    try {
      final SortedMap<Instant, Version> allVersions = new TreeMap<>(Comparator.reverseOrder());

      for (int i = 0; true; i += 10) {
        @Cleanup("disconnect")
        HttpsURLConnection connection = getConnectionForCall(VERSION_CALL + i, plugin);
        connection.connect();

        if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
          return Optional.empty();
        }

        final JsonArray versions =
            gson.fromJson(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8),
                    JsonObject.class)
                .get("result")
                .getAsJsonArray();

        for (JsonElement version : versions) {
          final Instant instant =
              Instant.parse(version.getAsJsonObject().get("created_at").getAsString());
          final Version pluginVersion =
              new Version(version.getAsJsonObject().get("name").getAsString());

          allVersions.put(instant, pluginVersion);
        }

        if (versions.size() < 10) {
          break;
        }
      }

      final DateTimeFormatter formatter =
          DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

      logDebug(
          PluginMessages.LOG_AVAILABLE_VERSIONS.getMessageRaw(
              ImmutableMap.of(
                  "plugin",
                  PluginContainerUtil.getPluginString(plugin),
                  "versions",
                  allVersions.entrySet().stream()
                      .map(
                          entry ->
                              formatter.format(entry.getKey()) + ": " + entry.getValue().getInput())
                      .collect(Collectors.joining("\n\t", "\t", "")))));

      return Optional.of(allVersions);
    } catch (SocketTimeoutException e) {
      printNetworkError(plugin, e);
    } catch (ClassCastException | IOException | URISyntaxException | DateTimeParseException e) {
      printErrorMessage(plugin, e);
    }

    return Optional.empty();
  }

  public static Optional<PluginVersionInfo> getPluginVersionInfo(PluginContainer plugin) {
    final Optional<Version> recommendedVersion = getRecommendedVersion(plugin);

    if (!recommendedVersion.isPresent()) {
      return Optional.empty();
    }

    final Optional<SortedMap<Instant, Version>> allVersions = getAllVersions(plugin);

    if (!allVersions.isPresent()) {
      return Optional.empty();
    }

    return Optional.of(new PluginVersionInfo(plugin, recommendedVersion.get(), allVersions.get()));
  }

  private static HttpsURLConnection getConnectionForCall(String call)
      throws ClassCastException, IOException, URISyntaxException {
    String urlStr = API_URL + call;

    logTrace(PluginMessages.LOG_CONTACTING_URL.getMessageRaw(ImmutableMap.of("url", urlStr)));

    URL url = new URL(urlStr);
    // Verify URL
    url.toURI();

    final HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
    applyDefaultSettings(httpsConnection);

    return httpsConnection;
  }

  private static HttpsURLConnection getConnectionForCall(String call, PluginContainer plugin)
      throws ClassCastException, IOException, URISyntaxException {
    return getConnectionForCall(PluginContainerUtil.replacePluginPlaceHolders(call, plugin));
  }

  private static void applyDefaultSettings(HttpsURLConnection connection) throws ProtocolException {
    final int defaultTimeout = AuraUpdateChecker.getConfig().getTiming().getConnectionTimeout();

    connection.setRequestMethod("GET");
    connection.setConnectTimeout(defaultTimeout);
    connection.setReadTimeout(defaultTimeout);
    connection.setUseCaches(false);
    connection.setInstanceFollowRedirects(true);
    if (authHeader != null) connection.setRequestProperty("Authorization", authHeader);
    connection.setRequestProperty(
        "User-Agent",
        "Java - Minecraft Sponge Plugin: "
            + AuraUpdateChecker.NAME
            + '/'
            + AuraUpdateChecker.VERSION);
  }

  private static void printErrorMessage(PluginContainer plugin, Throwable e) {
    final Logger logger = AuraUpdateChecker.getLogger();
    final String message =
        PluginMessages.LOG_CONTACTING_ERROR.getMessageRaw(
            ImmutableMap.of("plugin", PluginContainerUtil.getPluginString(plugin)));

    if (errorCounter.incrementAndGet() == 1) {
      logWarn(message, e);
    } else {
      logger.warn(message, e);
    }
  }

  private static void printNetworkError(PluginContainer plugin, Throwable e) {
    final Logger logger = AuraUpdateChecker.getLogger();
    final String message =
        PluginMessages.LOG_CONTACTING_ERROR.getMessageRaw(
            ImmutableMap.of("plugin", PluginContainerUtil.getPluginString(plugin)));

    logger.warn(message);

    if (AuraUpdateChecker.getConfig().getGeneral().getDebug()) {
      logger.info("[Debug]: " + message, e);
    } else {
      logger.debug(message, e);
    }
  }

  private static void logWarn(String message, Throwable e) {
    final Logger logger = AuraUpdateChecker.getLogger();

    if (AuraUpdateChecker.getConfig().getGeneral().getDebug()) {
      logger.warn(message, e);
    } else {
      logger.debug(message, e);
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
