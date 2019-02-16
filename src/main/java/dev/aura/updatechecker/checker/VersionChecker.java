package dev.aura.updatechecker.checker;

import com.google.common.collect.ImmutableMap;
import dev.aura.updatechecker.AuraUpdateChecker;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

@RequiredArgsConstructor
public class VersionChecker {
  private final Collection<PluginContainer> availablePlugins;

  private List<PluginContainer> checkablePlugins = null;
  private final List<Task> scheduledTasks = new LinkedList<>();
  private final AtomicBoolean active = new AtomicBoolean(false);

  @Getter private boolean notifyAdmins = false;
  @Getter private ImmutableMap<PluginContainer, PluginVersionInfo> versionInfo = ImmutableMap.of();

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

    logger.info("Updates available for:");

    for (Map.Entry<PluginContainer, PluginVersionInfo> entry : versionInfo.entrySet()) {
      final PluginVersionInfo pluginVersionInfo = entry.getValue();

      if (pluginVersionInfo.isUpToDate()) {
        continue;
      }

      final PluginContainer plugin = entry.getKey();
      String message =
          PluginContainerUtil.getPluginString(plugin)
              + ":\n\tCurrent version: "
              + pluginVersionInfo.getCurrentVersion().getInput()
              + "\n\t";

      if (pluginVersionInfo.isNewRecommended()) {
        message +=
            "\n\tRecommended version: " + pluginVersionInfo.getRecommendedVersion().getInput();
      }
      if (pluginVersionInfo.isNewLatest()) {
        message += "\n\tLatest version: " + pluginVersionInfo.getLatestVersion().getInput();
      }

      logger.info(message);
    }
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
