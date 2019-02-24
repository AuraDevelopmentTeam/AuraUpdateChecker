package dev.aura.updatechecker;

import dev.aura.updatechecker.config.Config;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBase {
  protected static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(AuraUpdateChecker.NAME);
  protected static final Config DEFAULT_CONFIG = new Config();

  @BeforeClass
  public static void setupPlugin() {
    if (AuraUpdateChecker.getInstance() == null) {
      final AuraUpdateChecker instance = new AuraUpdateChecker();

      instance.logger = DEFAULT_LOGGER;
      instance.config = DEFAULT_CONFIG;
    }
  }
}
