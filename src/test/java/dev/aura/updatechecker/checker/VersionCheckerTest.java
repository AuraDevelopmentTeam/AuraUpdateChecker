package dev.aura.updatechecker.checker;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;

public class VersionCheckerTest {
  @Test
  public void allErrorTest() {
    VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##")));

    assertEquals("Expected 3 errors", Optional.of(3), checker.checkForPluginAvailability());
  }

  @Test
  public void availabilityTest() {
    VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer("invsync"),
                new DummyPluginContainer("notavailablegfndfngkd"),
                new DummyPluginContainer("error!&!&&!&##")));

    assertEquals("Expected 1 error", Optional.of(1), checker.checkForPluginAvailability());
  }
}
