package dev.aura.updatechecker.checker;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
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

    checker.checkForPluginAvailability();

    assertEquals("Expected 3 errors", 3, OreAPI.getErrorCounter());
  }

  @Test
  public void availabilityTest() {
    VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer("invsync"),
                new DummyPluginContainer("notavailablegfndfngkd"),
                new DummyPluginContainer("error!&!&&!&##")));

    checker.checkForPluginAvailability();

    assertEquals("Expected 1 error", 1, OreAPI.getErrorCounter());
  }
}
