package dev.aura.updatechecker.test.checker;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import dev.aura.updatechecker.checker.OreAPI;
import dev.aura.updatechecker.checker.VersionChecker;

public class VersionCheckerTest {
    @Test
    public void availabilityTest() {
        VersionChecker checker = new VersionChecker(Arrays.asList(new DummyPluginContainer("invsync"),
                new DummyPluginContainer("notavailablegfndfngkd"), new DummyPluginContainer("error!&!&&!&##")));

        checker.checkForPluginAvailability();

        assertEquals("Expected 1 error", 1, OreAPI.getErrorCounter());
    }

    @Test
    public void allErrorTest() {
        VersionChecker checker = new VersionChecker(Arrays.asList(new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##"), new DummyPluginContainer("error!&!&&!&##")));

        checker.checkForPluginAvailability();

        assertEquals("Expected 3 errors", 3, OreAPI.getErrorCounter());
    }
}
