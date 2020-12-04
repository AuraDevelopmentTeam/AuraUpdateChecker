package dev.aura.updatechecker;

import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.lib.messagestranslator.unittesthelper.UnitTestMessagesTranslator;
import dev.aura.updatechecker.config.Config;
import org.bstats.sponge.MetricsLite2;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBase {
  protected static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(AuraUpdateChecker.NAME);
  protected static final Config DEFAULT_CONFIG =
      new Config(new Config.General(), new Config.Timing().setConnectionTimeout(10_000));
  protected static final MessagesTranslator DEFAULT_TRANSLATOR =
      new UnitTestMessagesTranslator(AuraUpdateChecker.ID);

  @BeforeClass
  public static void setupPlugin() {
    if (AuraUpdateChecker.getInstance() == null) {
      final AuraUpdateChecker instance =
          new AuraUpdateChecker(Mockito.mock(MetricsLite2.Factory.class));

      instance.logger = DEFAULT_LOGGER;
      instance.config = DEFAULT_CONFIG;
      instance.translator = DEFAULT_TRANSLATOR;
    }
  }
}
