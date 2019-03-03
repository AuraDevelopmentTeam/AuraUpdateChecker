package dev.aura.updatechecker.checker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.config.Config;
import dev.aura.updatechecker.message.PluginMessages;
import dev.aura.updatechecker.permission.PermissionRegistry;
import dev.aura.updatechecker.util.PluginContainerUtil;
import dev.aura.updatechecker.util.PluginVersionInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

@RequiredArgsConstructor
public class VersionChecker {
  private final Collection<PluginContainer> availablePlugins;
  private final Config config;

  @VisibleForTesting List<PluginContainer> checkablePlugins = null;
  private final List<Task> scheduledTasks = new LinkedList<>();
  private final AtomicBoolean active = new AtomicBoolean(false);

  @Getter private ImmutableMap<PluginContainer, PluginVersionInfo> versionInfo = ImmutableMap.of();
  @Getter private String updateMessage = "";
  @Getter @VisibleForTesting PaginationList updateMessagePagination = null;

  public void start() {
    active.set(true);

    // Starting new task to discover checkable plugins
    final Task task =
        startTask(
            Task.builder()
                .execute(this::checkForPluginAvailabilityTask)
                .delay(5, TimeUnit.SECONDS)
                .async()
                .name(AuraUpdateChecker.ID + "-availablity-check"));

    if (task != null) {
      scheduledTasks.add(task);
    }
  }

  public void stop() {
    active.set(false);

    scheduledTasks.forEach(Task::cancel);
  }

  public Optional<Integer> checkForPluginAvailability() {
    final Logger logger = AuraUpdateChecker.getLogger();

    logDebug(logger, PluginMessages.LOG_STARTING_CHECKS.getMessageRaw());

    if (checkablePlugins != null) {
      logger.info(PluginMessages.LOG_ALREADY_CHECKED.getMessageRaw());

      return Optional.empty();
    }

    OreAPI.resetErrorCounter();

    checkablePlugins =
        availablePlugins
            .stream()
            .filter(
                plugin -> {
                  final String pluginName = PluginContainerUtil.getPluginString(plugin);
                  logTrace(
                      logger,
                      PluginMessages.LOG_STARTED_PLUGIN_CHECK.getMessageRaw(
                          ImmutableMap.of("plugin", pluginName)));

                  final boolean isOnOre = OreAPI.isOnOre(plugin);

                  if (isOnOre) {
                    logDebug(
                        logger,
                        PluginMessages.LOG_PLUGIN_ON_ORE.getMessageRaw(
                            ImmutableMap.of("plugin", pluginName)));
                  } else {
                    logDebug(
                        logger,
                        PluginMessages.LOG_PLUGIN_NOT_ON_ORE.getMessageRaw(
                            ImmutableMap.of("plugin", pluginName)));
                  }

                  return isOnOre;
                })
            .collect(Collectors.toList());

    if (OreAPI.getErrorCounter() >= availablePlugins.size()) {
      logger.warn(PluginMessages.LOG_INTERNET_DOWN.getMessageRaw());
      logger.info(PluginMessages.LOG_RUN_RELOAD.getMessageRaw());
    }

    logDebug(logger, PluginMessages.LOG_FINISHED_CHECKS.getMessageRaw());
    logDebug(
        logger,
        PluginMessages.LOG_AVAILABLE_COUNT.getMessageRaw(
            ImmutableMap.of("count", Integer.toString(checkablePlugins.size()))));

    return Optional.of(OreAPI.getErrorCounter());
  }

  public void checkForPluginAvailabilityTask(Task self) {
    final int errorCount = checkForPluginAvailability().orElse(Integer.MAX_VALUE);

    if (errorCount >= availablePlugins.size()) {
      active.set(false);
    }

    final Task task =
        startTask(
            Task.builder()
                .execute(this::checkForPluginUpdatesTask)
                .delay(5, TimeUnit.SECONDS)
                .interval(config.getTiming().getUpdateVersionInfoInterval(), TimeUnit.MINUTES)
                .async()
                .name(AuraUpdateChecker.ID + "-update-check"));

    if (task != null) {
      scheduledTasks.add(task);
    }

    scheduledTasks.remove(self);
  }

  public boolean checkForPluginUpdates() {
    final Logger logger = AuraUpdateChecker.getLogger();

    logDebug(logger, PluginMessages.LOG_START_FETCHING.getMessageRaw());

    final Map<PluginContainer, PluginVersionInfo> newVersionInfo = new HashMap<>();

    for (PluginContainer plugin : checkablePlugins) {
      final Optional<PluginVersionInfo> pluginInfo = OreAPI.getPluginVersionInfo(plugin);

      if (pluginInfo.isPresent()) {
        newVersionInfo.put(plugin, pluginInfo.get());
      }
    }

    logDebug(logger, PluginMessages.LOG_FINISHED_FETCHING.getMessageRaw());

    if (newVersionInfo.equals(versionInfo)) {
      return false;
    }

    versionInfo = ImmutableMap.copyOf(newVersionInfo);

    return true;
  }

  public void checkForPluginUpdatesTask(Task self) {
    final boolean versionsChanged = checkForPluginUpdates();

    if (!versionsChanged) {
      return;
    }

    boolean updatesAvailable = false;

    for (PluginVersionInfo pluginInfo : versionInfo.values()) {
      if (!pluginInfo.isUpToDate()) {
        updatesAvailable = true;
        break;
      }
    }

    if (!updatesAvailable) {
      return;
    }

    StringBuilder message = new StringBuilder();
    Map<String, String> replacements = new HashMap<String, String>();

    for (Map.Entry<PluginContainer, PluginVersionInfo> entry : versionInfo.entrySet()) {
      final PluginVersionInfo pluginVersionInfo = entry.getValue();

      if (pluginVersionInfo.isUpToDate()) {
        continue;
      }

      final PluginContainer plugin = entry.getKey();

      replacements.put("plugin", PluginContainerUtil.getPluginString(plugin));
      replacements.put(
          "installed",
          PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_INSTALLED.getMessageRaw(
              ImmutableMap.of("version", pluginVersionInfo.getInstalledVersion().getInput())));
      replacements.put(
          "recommended",
          pluginVersionInfo.isNewRecommended()
              ? PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_RECOMMENDED.getMessageRaw(
                  ImmutableMap.of("version", pluginVersionInfo.getRecommendedVersion().getInput()))
              : "");
      replacements.put(
          "latest",
          pluginVersionInfo.isNewLatest()
              ? PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_LATEST.getMessageRaw(
                  ImmutableMap.of("version", pluginVersionInfo.getLatestVersion().getInput()))
              : "");

      message.append(PluginMessages.NOTIFICATION_UPDATE_AVAILABLE.getMessageRaw(replacements));
      replacements.clear();
    }

    updateMessage = message.toString();

    updateMessagePagination =
        PaginationList.builder()
            .padding(PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_PADDING.getMessage())
            .title(PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_TITLE.getMessage())
            .contents(TextSerializers.FORMATTING_CODE.deserialize(updateMessage))
            .build();

    // Inform all admins and console
    showUpdateMessage(Sponge.getServer().getConsole());
    Sponge.getServer().getOnlinePlayers().stream().forEach(this::showUpdateMessage);
  }

  public boolean canShowUpdateMessage(MessageReceiver messageReceiver) {
    return (messageReceiver != null)
        && (updateMessagePagination != null)
        && (!(messageReceiver instanceof Player)
            || (((Player) messageReceiver).isOnline()
                && ((Player) messageReceiver)
                    .hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_JOIN)));
  }

  public void showUpdateMessage(MessageReceiver messageReceiver) {
    if (canShowUpdateMessage(messageReceiver)) {
      if (messageReceiver instanceof Player) {
        updateMessagePagination.sendTo(messageReceiver);
      } else {
        messageReceiver.sendMessage(
            TextSerializers.FORMATTING_CODE.deserialize(
                PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_TITLE.getMessageRaw()
                    + '\n'
                    + updateMessage));
      }
    }
  }

  @Nullable
  private Task startTask(Task.Builder taskBuilder) {
    if (!active.get()) {
      return null;
    }

    final Task task = taskBuilder.submit(AuraUpdateChecker.getInstance());

    AuraUpdateChecker.getLogger()
        .debug(
            PluginMessages.LOG_STARTED_TASK.getMessageRaw(
                ImmutableMap.of("count", task.getName())));

    return task;
  }

  private void logDebug(Logger logger, String message) {
    if (config.getGeneral().getDebug()) {
      logger.info("[Debug]: " + message);
    } else {
      logger.debug(message);
    }
  }

  private void logTrace(Logger logger, String message) {
    if (config.getGeneral().getDebug()) {
      logger.info("[Trace]: " + message);
    } else {
      logger.trace(message);
    }
  }
}
