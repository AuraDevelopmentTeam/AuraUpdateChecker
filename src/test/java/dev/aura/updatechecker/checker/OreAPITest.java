package dev.aura.updatechecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dev.aura.updatechecker.TestApi;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.plugin.PluginContainer;

public class OreAPITest extends TestApi {
  @Before
  @After
  public void resetCounter() {
    OreAPI.resetErrorCounter();
  }

  @Test
  public void availablityTest() {
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

    assertEquals("No errors should have happend", 0, OreAPI.getErrorCounter());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void constructorTest() throws Throwable {
    try {
      Constructor<OreAPI> contructor = OreAPI.class.getDeclaredConstructor();
      contructor.setAccessible(true);
      contructor.newInstance();
    } catch (InvocationTargetException e) {
      if (e.getCause().getClass() == UnsupportedOperationException.class) throw e.getCause();
      else throw e;
    }
  }

  @Test
  public void errorTest()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
          IllegalAccessException {
    final int count = 10;
    final PluginContainer errorContainer = new DummyPluginContainer(ERROR_PROJECT1);

    for (int i = 0; i < count; ++i) {
      assertFalse(
          "Expected \"" + ERROR_PROJECT1 + "\" not to be available",
          OreAPI.isOnOre(errorContainer));
    }

    assertEquals(count + " errors should have happend", count, OreAPI.getErrorCounter());
  }
}
