package dev.aura.updatechecker;

import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;

public class TestApi extends TestBase {
  @ClassRule
  public static HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(SimulationSource.defaultPath("simulation.json"));
}
