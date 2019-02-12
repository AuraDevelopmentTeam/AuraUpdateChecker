package dev.aura.updatechecker;

import com.google.inject.Inject;
import dev.aura.updatechecker.checker.VersionChecker;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.NonNull;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

@Plugin(
  id = AuraUpdateChecker.ID,
  name = AuraUpdateChecker.NAME,
  version = AuraUpdateChecker.VERSION,
  description = AuraUpdateChecker.DESCRIPTION,
  url = AuraUpdateChecker.URL,
  authors = {AuraUpdateChecker.AUTHOR_BRAINSTONE}
)
public class AuraUpdateChecker {
  public static final String ID = "@id@";
  public static final String NAME = "@name@";
  public static final String VERSION = "@version@";
  public static final String DESCRIPTION = "@description@";
  public static final String URL = "https://github.com/AuraDevelopmentTeam/AuraUpdateChecker";
  public static final String AUTHOR_BRAINSTONE = "The_BrainStone";

  @NonNull @Getter protected static AuraUpdateChecker instance = null;

  @Inject protected Metrics2 metrics;
  @Inject @NonNull protected Logger logger;

  @Inject
  @DefaultConfig(sharedRoot = false)
  @NonNull
  protected Path configFile;

  @Inject
  @ConfigDir(sharedRoot = false)
  @NonNull
  protected Path configDir;

  @NonNull protected VersionChecker versionChecker;

  public static Logger getLogger() {
    if ((instance == null) || (instance.logger == null)) return NOPLogger.NOP_LOGGER;
    else return instance.logger;
  }

  public static Path getConfigFile() {
    return instance.configFile;
  }

  public static Path getConfigDir() {
    return instance.configDir;
  }

  public static VersionChecker getVersionChecker() {
    return instance.versionChecker;
  }

  @Listener
  public void gameConstruct(GameConstructionEvent event) {
    instance = this;
  }

  @Listener
  public void init(GameInitializationEvent event) {
    logger.info("Initializing " + NAME + " Version " + VERSION);

    if (VERSION.contains("SNAPSHOT")) {
      logger.warn("WARNING! This is a snapshot version!");
      logger.warn("Use at your own risk!");
    }
    if (VERSION.contains("DEV")) {
      logger.info("This is a unreleased development version!");
      logger.info("Things might not work properly!");
    }

    versionChecker = new VersionChecker(Sponge.getPluginManager().getPlugins());

    logger.info("Loaded successfully!");
  }

  @Listener
  public void loadComplete(GameLoadCompleteEvent event) {
    Task task =
        Task.builder()
            .execute(() -> versionChecker.checkForPluginAvailability())
            .delay(5, TimeUnit.SECONDS)
            .async()
            .name(ID + "-availablity-check")
            .submit(this);

    logger.debug("Started \"" + task.getName() + '"');
  }

  @Listener
  public void reload(GameReloadEvent event) throws Exception {
    Cause cause = Cause.source(this).build();

    // Unregistering everything
    GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
    stop(gameStoppingEvent);

    // Starting over
    GameInitializationEvent gameInitializationEvent =
        SpongeEventFactory.createGameInitializationEvent(cause);
    init(gameInitializationEvent);
    GameLoadCompleteEvent gameLoadCompleteEvent =
        SpongeEventFactory.createGameLoadCompleteEvent(cause);
    loadComplete(gameLoadCompleteEvent);

    logger.info("Reloaded successfully!");
  }

  @Listener
  public void stop(GameStoppingEvent event) throws Exception {
    logger.info("Shutting down " + NAME + " Version " + VERSION);

    // TODO

    logger.info("Unloaded successfully!");
  }
}
