package dev.aura.updatechecker.checker;

import com.google.common.collect.ImmutableMap;
import dev.aura.updatechecker.AuraUpdateChecker;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.serializer.TextSerializers;

@RequiredArgsConstructor
public class VersionChecker {
  private final Collection<PluginContainer> availablePlugins;

  private List<PluginContainer> checkablePlugins = null;
  private final List<Task> scheduledTasks = new LinkedList<>();
  private final AtomicBoolean active = new AtomicBoolean(false);

  @Getter private boolean notifyAdmins = false;
  @Getter private ImmutableMap<PluginContainer, PluginVersionInfo> versionInfo = ImmutableMap.of();
  @Getter private String updateMessage = "";
  @Getter private PaginationList updateMessagePagination = null;

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

    logger.debug("Start checking plugins for availability on Ore Repository...");

    if (checkablePlugins != null) {
      logger.info("Already checked plugins for availability!");

      return Optional.empty();
    }

    OreAPI.resetErrorCounter();

    checkablePlugins =
        availablePlugins
            .stream()
            .filter(
                plugin -> {
                  final String pluginName = PluginContainerUtil.getPluginString(plugin);
                  logger.trace(
                      "Started checking if plugin "
                          + pluginName
                          + " is available on Ore Repository.");

                  final boolean isOnOre = OreAPI.isOnOre(plugin);

                  if (isOnOre) {
                    logger.debug("Plugin " + pluginName + " is available on Ore Repository.");
                  } else {
                    logger.trace("Plugin " + pluginName + " is NOT available on Ore Repository.");
                  }

                  return isOnOre;
                })
            .collect(Collectors.toList());

    if (OreAPI.getErrorCounter() >= availablePlugins.size()) {
      logger.warn(
          "It appears that your internet connection is down or not working properly, because all HTTPS requests failed.");
      logger.info("If it is working again, run \"/uc reload\", to reenable update checking.");
    }

    logger.debug("Finished checking plugins for availability on Ore Repository!");
    logger.debug(checkablePlugins.size() + " plugins available for update checks!");

    return Optional.of(OreAPI.getErrorCounter());
  }

  public void checkForPluginUpdates() {
    final Logger logger = AuraUpdateChecker.getLogger();

    logger.debug("Start fetching plugin versions from the Ore Repository...");

    final Map<PluginContainer, PluginVersionInfo> newVersionInfo = new HashMap<>();
    boolean updatesAvailable = false;

    for (PluginContainer plugin : checkablePlugins) {
      final Optional<PluginVersionInfo> pluginInfo = OreAPI.getPluginVersionInfo(plugin);

      if (pluginInfo.isPresent()) {
        newVersionInfo.put(plugin, pluginInfo.get());
        updatesAvailable = updatesAvailable || !pluginInfo.get().isUpToDate();
      }
    }

    logger.debug("Finished fetching plugin versions from the Ore Repository!");

    if (newVersionInfo.equals(versionInfo)) {
      return;
    }

    notifyAdmins = true;
    versionInfo = ImmutableMap.copyOf(newVersionInfo);

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
          "current",
          PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_CURRENT.getMessageRaw(
              ImmutableMap.of("version", pluginVersionInfo.getCurrentVersion().getInput())));
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
    logger.info(
        PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_TITLE.getMessageRaw() + '\n' + updateMessage);

    updateMessagePagination =
        PaginationList.builder()
            .padding(PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_PADDING.getMessage())
            .title(PluginMessages.NOTIFICATION_UPDATE_AVAILABLE_TITLE.getMessage())
            .contents(TextSerializers.FORMATTING_CODE.deserialize(updateMessage))
            .build();

    // Inform all admins
    Sponge.getServer()
        .getOnlinePlayers()
        .stream()
        .filter(
            player ->
                player.hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_PERIODIC))
        .forEach(updateMessagePagination::sendTo);
  }

  public void checkForPluginUpdatesTask(Task self) {
    checkForPluginUpdates();
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
                .interval(30, TimeUnit.MINUTES)
                .async()
                .name(AuraUpdateChecker.ID + "-update-check"));

    if (task != null) {
      scheduledTasks.add(task);
    }

    scheduledTasks.remove(self);
  }

  @Nullable
  private Task startTask(Task.Builder taskBuilder) {
    if (!active.get()) {
      return null;
    }

    final Task task = taskBuilder.submit(AuraUpdateChecker.getInstance());

    AuraUpdateChecker.getLogger().debug("Started task \"" + task.getName() + '"');

    return task;
  }
}
