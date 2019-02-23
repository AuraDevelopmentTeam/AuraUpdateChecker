package dev.aura.updatechecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.config.Config;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import java.util.Arrays;
import java.util.Optional;
import org.junit.ClassRule;
import org.junit.Test;

public class VersionCheckerTest {
  @ClassRule
  public static HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(SimulationSource.defaultPath("simulation.json"));

  @Test
  public void allErrorAvailabilitTest() {
    final VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##")),
            Config.DEFAULT_CONFIG);

    assertEquals("Expected 3 errors", Optional.of(3), checker.checkForPluginAvailability());
  }

  @Test
  public void availabilityTest() {
    final VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer(AuraUpdateChecker.ID),
                new DummyPluginContainer("notavailablegfndfngkd"),
                new DummyPluginContainer("error!&!&&!&##")),
            Config.DEFAULT_CONFIG);

    assertEquals("Expected 1 error", Optional.of(1), checker.checkForPluginAvailability());
  }

  @Test
  public void versionTest() {
    final VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer(AuraUpdateChecker.ID),
                new DummyPluginContainer("nucleus")),
            Config.DEFAULT_CONFIG);

    // No errors here
    assertEquals("Expected 0 errors", Optional.of(0), checker.checkForPluginAvailability());

    // First check should change the data
    assertTrue(checker.checkForPluginUpdates());
    // Second shouldn't
    assertFalse(checker.checkForPluginUpdates());
  }
}
