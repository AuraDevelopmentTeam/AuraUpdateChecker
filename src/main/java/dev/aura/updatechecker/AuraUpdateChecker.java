package dev.aura.updatechecker;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.updatechecker.checker.VersionChecker;
import dev.aura.updatechecker.command.CommandBase;
import dev.aura.updatechecker.config.Config;
import dev.aura.updatechecker.event.PlayerEvents;
import dev.aura.updatechecker.permission.PermissionRegistry;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

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

  @NonNull @Getter private static AuraUpdateChecker instance = null;

  @Inject @NonNull protected PluginContainer container;
  @Inject protected Metrics2 metrics;
  @Inject @NonNull protected Logger logger;

  @Inject protected GuiceObjectMapperFactory factory;

  @Inject
  @DefaultConfig(sharedRoot = false)
  protected ConfigurationLoader<CommentedConfigurationNode> loader;

  @Inject
  @ConfigDir(sharedRoot = false)
  @NonNull
  protected Path configDir;

  @NonNull protected Config config;
  protected PermissionRegistry permissionRegistry;
  @NonNull protected VersionChecker versionChecker;
  @NonNull protected MessagesTranslator translator;

  protected List<Object> eventListeners = new LinkedList<>();

  public AuraUpdateChecker() {
    if (instance != null) throw new IllegalStateException("Instance already exists!");

    instance = this;
  }

  public static Logger getLogger() {
    if ((instance == null) || (instance.logger == null)) return NOPLogger.NOP_LOGGER;
    else return instance.logger;
  }

  public static Path getConfigDir() {
    return instance.configDir;
  }

  public static VersionChecker getVersionChecker() {
    return instance.versionChecker;
  }

  public static Config getConfig() {
    if ((instance == null) || (instance.logger == null)) return Config.DEFAULT_CONFIG;
    else return instance.config;
  }

  public static MessagesTranslator getTranslator() {
    if ((instance == null) || (instance.translator == null)) return null;
    else return instance.translator;
  }

  @Listener
  public void init(GameInitializationEvent event) throws IOException, ObjectMappingException {
    logger.info("Initializing " + NAME + " Version " + VERSION);

    if (VERSION.contains("SNAPSHOT")) {
      logger.warn("WARNING! This is a snapshot version!");
      logger.warn("Use at your own risk!");
    }
    if (VERSION.contains("DEV")) {
      logger.info("This is a unreleased development version!");
      logger.info("Things might not work properly!");
    }

    loadConfig();

    if (permissionRegistry == null) {
      permissionRegistry = new PermissionRegistry(this);
      logger.debug("Registered permissions");
    }

    versionChecker = new VersionChecker(Sponge.getPluginManager().getPlugins(), config);
    translator =
        new MessagesTranslator(
            new File(getConfigDir().toFile(), "lang"), config.getGeneral().getLanguage(), this, ID);

    CommandBase.register(this);
    logger.debug("Registered commands");

    addEventListener(new PlayerEvents());
    logger.debug("Registered events");

    logger.info("Loaded successfully!");
  }

  private void loadConfig() throws IOException, ObjectMappingException {
    final TypeToken<Config> configToken = TypeToken.of(Config.class);

    logger.debug("Loading config...");

    CommentedConfigurationNode node =
        loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory));

    config = node.<Config>getValue(configToken, Config::new);

    logger.debug("Saving/Formatting config...");
    node.setValue(configToken, config);
    loader.save(node);
  }

  @Listener
  public void serverStarted(GameStartedServerEvent event) {
    versionChecker.start();
  }

  @Listener
  public void reload(GameReloadEvent event) throws Exception {
    Cause cause =
        Cause.builder()
            .append(this)
            .build(EventContext.builder().add(EventContextKeys.PLUGIN, container).build());

    // Unregistering everything
    GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
    stop(gameStoppingEvent);

    // Starting over
    GameInitializationEvent gameInitializationEvent =
        SpongeEventFactory.createGameInitializationEvent(cause);
    init(gameInitializationEvent);
    GameStartedServerEvent gameStartedServerEvent =
        SpongeEventFactory.createGameStartedServerEvent(cause);
    serverStarted(gameStartedServerEvent);

    logger.info("Reloaded successfully!");
  }

  @Listener
  public void stop(GameStoppingEvent event) throws Exception {
    logger.info("Shutting down " + NAME + " Version " + VERSION);

    if (versionChecker != null) {
      versionChecker.stop();
    }

    removeCommands();
    logger.debug("Unregistered commands");

    removeEventListeners();
    logger.debug("Unregistered events");

    config = null;
    logger.debug("Unloaded config");

    logger.info("Unloaded successfully!");
  }

  private void addEventListener(Object listener) {
    eventListeners.add(listener);

    Sponge.getEventManager().registerListeners(this, listener);
  }

  private void removeCommands() {
    final CommandManager commandManager = Sponge.getCommandManager();

    commandManager.getOwnedBy(this).forEach(commandManager::removeMapping);
  }

  private void removeEventListeners() throws Exception {
    for (Object listener : eventListeners) {
      Sponge.getEventManager().unregisterListeners(listener);

      if (listener instanceof AutoCloseable) {
        ((AutoCloseable) listener).close();
      }
    }

    eventListeners.clear();
  }
}
