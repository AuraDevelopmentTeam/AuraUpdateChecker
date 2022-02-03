package dev.aura.updatechecker.message;

import dev.aura.lib.messagestranslator.Message;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.updatechecker.AuraUpdateChecker;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@RequiredArgsConstructor
public enum PluginMessages implements Message {
  // Admin Messages
  ADMIN_RELOAD_SUCCESSFUL("reloadSuccessful"),
  ADMIN_RELOAD_NOT_SUCCESSFUL("reloadNotSuccessful"),
  // Notifications
  NOTIFICATION_UPDATE_AVAILABLE_PADDING("updateAvailablePadding"),
  NOTIFICATION_UPDATE_AVAILABLE_TITLE("updateAvailableTitle"),
  NOTIFICATION_UPDATE_AVAILABLE("updateAvailable"),
  NOTIFICATION_UPDATE_AVAILABLE_INSTALLED("updateAvailableInstalled"),
  NOTIFICATION_UPDATE_AVAILABLE_RECOMMENDED("updateAvailableRecommended"),
  NOTIFICATION_UPDATE_AVAILABLE_LATEST("updateAvailableLatest"),
  NOTIFICATION_NO_UPDATE_AVAILABLE("noUpdateAvailable"),
  // Log Messages
  LOG_RECOMMENDED_VERSION("recommendedVersion"),
  LOG_AVAILABLE_VERSIONS("availableVersions"),
  LOG_CONTACTING_URL("contactingURL"),
  LOG_CONTACTING_ERROR("contactingError"),
  LOG_STARTING_CHECKS("startingChecks"),
  LOG_ALREADY_CHECKED("alreadyChecked"),
  LOG_STARTED_PLUGIN_CHECK("startedPluginCheck"),
  LOG_PLUGIN_ON_ORE("pluginOnOre"),
  LOG_PLUGIN_NOT_ON_ORE("pluginNotOnOre"),
  LOG_INTERNET_DOWN("internetDown"),
  LOG_RUN_RELOAD("runReload"),
  LOG_FINISHED_CHECKS("finishedChecks"),
  LOG_AVAILABLE_COUNT("availableCount"),
  LOG_START_FETCHING("startFetching"),
  LOG_FINISHED_FETCHING("finishedFetching"),
  LOG_STARTED_TASK("startedTask");

  @Getter private final String stringPath;

  public Text getMessage() {
    return getMessage(null);
  }

  public Text getMessage(Map<String, String> replacements) {
    final String message = getMessageRaw(replacements);

    return TextSerializers.FORMATTING_CODE.deserialize(message);
  }

  public String getMessageRaw() {
    return getMessageRaw(null);
  }

  public String getMessageRaw(Map<String, String> replacements) {
    final MessagesTranslator translator = AuraUpdateChecker.getTranslator();

    if (translator == null) {
      return getStringPath();
    } else {
      return translator.translateWithFallback(this, replacements);
    }
  }
}
