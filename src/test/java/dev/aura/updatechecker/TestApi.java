package dev.aura.updatechecker;

import com.google.common.collect.ImmutableList;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.core.config.LogLevel;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;

public class TestApi extends TestBase {
  @ClassRule
  public static HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(
          SimulationSource.defaultPath("simulation.json"),
          HoverflyConfig.localConfigs().logLevel(LogLevel.DEBUG));

  protected static final String PROJECT1 = "project1";
  protected static final String PROJECT2 = "project2";
  protected static final String PROJECT3 = "project3";
  protected static final ImmutableList<String> PROJECTS =
      ImmutableList.of(PROJECT1, PROJECT2, PROJECT3);

  protected static final String MISSING_PROJECT1 = "missing_project1";
  protected static final String MISSING_PROJECT2 = "missing_project2";
  protected static final String MISSING_PROJECT3 = "missing_project3";
  protected static final ImmutableList<String> MISSING_PROJECTS =
      ImmutableList.of(MISSING_PROJECT1, MISSING_PROJECT2, MISSING_PROJECT3);

  protected static final String ERROR_PROJECT1 = "spaces are bad";
  protected static final String ERROR_PROJECT2 = "!!7678&%/(!&7867(%!&%78'#235#2?";
  protected static final String ERROR_PROJECT3 = "error!&!&&!&##";
  protected static final ImmutableList<String> ERROR_PROJECTS =
      ImmutableList.of(ERROR_PROJECT1, ERROR_PROJECT2, ERROR_PROJECT3);
}
