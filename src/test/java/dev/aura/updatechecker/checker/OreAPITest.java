package dev.aura.updatechecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import dev.aura.updatechecker.util.PluginVersionInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import org.spongepowered.api.plugin.PluginContainer;

public class OreAPITest extends TestApi {
  @Test
  public void availabilityTest() {
    assertTrue("Expected to be able to authenticate", OreAPI.authenticate());

    for (String project : PROJECTS) {
      assertTrue(
          "Expected " + project + " to be available",
          OreAPI.isOnOre(new DummyPluginContainer(project)));
    }

    for (String project : MISSING_PROJECTS) {
      assertFalse(
          "Expected " + project + " not to be available",
          OreAPI.isOnOre(new DummyPluginContainer(project)));
    }

    assertEquals("No errors should have happened", 0, OreAPI.getErrorCounter());

    // +1 OK for the auth
    assertRequestCountMatch(PROJECTS.size() + 1L, MISSING_PROJECTS.size());
  }

  @Test
  public void versionTest() {
    assertTrue("Expected to be able to authenticate", OreAPI.authenticate());

    final PluginContainer upToDate = new DummyPluginContainer(PROJECT3, "3.3.4");
    final PluginContainer newLatest = new DummyPluginContainer(PROJECT3, "3.3.3");
    final PluginContainer newRecommended = new DummyPluginContainer(PROJECT2, "2.2.1");
    final PluginContainer newBoth = new DummyPluginContainer(PROJECT3, "3.3.2");

    assertSame(
        PluginVersionInfo.PluginStatus.UP_TO_DATE,
        OreAPI.getPluginVersionInfo(upToDate).map(PluginVersionInfo::getPluginStatus).orElse(null));
    assertSame(
        PluginVersionInfo.PluginStatus.NEW_LATEST,
        OreAPI.getPluginVersionInfo(newLatest)
            .map(PluginVersionInfo::getPluginStatus)
            .orElse(null));
    assertSame(
        PluginVersionInfo.PluginStatus.NEW_RECOMMENDED,
        OreAPI.getPluginVersionInfo(newRecommended)
            .map(PluginVersionInfo::getPluginStatus)
            .orElse(null));
    assertSame(
        PluginVersionInfo.PluginStatus.NEW_BOTH,
        OreAPI.getPluginVersionInfo(newBoth).map(PluginVersionInfo::getPluginStatus).orElse(null));

    assertEquals("No errors should have happened", 0, OreAPI.getErrorCounter());
    // 4 OKs per test (1 for main plugin, 3 for versions) + 1 OK for the auth
    assertRequestCountMatch(17L, 0L);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void constructorTest() throws Throwable {
    try {
      Constructor<OreAPI> constructor = OreAPI.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      if (e.getCause().getClass() == UnsupportedOperationException.class) throw e.getCause();
      else throw e;
    }
  }

  @Test
  public void errorTest() throws SecurityException, IllegalArgumentException {
    assertTrue("Expected to be able to authenticate", OreAPI.authenticate());

    final int count = 10;
    final PluginContainer errorContainer = new DummyPluginContainer(ERROR_PROJECT1);

    for (int i = 0; i < count; ++i) {
      assertFalse(
          "Expected \"" + ERROR_PROJECT1 + "\" not to be available",
          OreAPI.isOnOre(errorContainer));
    }

    assertEquals(count + " errors should have happened", count, OreAPI.getErrorCounter());
    // 1 OK for the auth
    assertRequestCountMatch(1L, 0L);
  }
}
