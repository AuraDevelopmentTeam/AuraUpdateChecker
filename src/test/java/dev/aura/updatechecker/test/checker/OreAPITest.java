package dev.aura.updatechecker.test.checker;

import static org.junit.Assert.*;

import org.junit.Test;
import org.spongepowered.api.plugin.PluginContainer;

import dev.aura.updatechecker.checker.OreAPI;

public class OreAPITest {
    @Test
    public void availablityTest() {
        final PluginContainer existingContainer = new DummyPluginContainer("invsync");
        final PluginContainer nonExistingContainer = new DummyPluginContainer(
                "thisIDdoesntexistbfjsdgbhjbhghhsfgdsghfh");

        assertTrue("Expected invsync to be available", OreAPI.isOnOre(existingContainer));
        assertFalse("Expected thisIDdoesntexistbfjsdgbhjbhghhsfgdsghfh not to be available",
                OreAPI.isOnOre(nonExistingContainer));
    }
}
