package dev.aura.updatechecker.test.version;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import dev.aura.updatechecker.version.VersionComponentType;

public class VersionComponentTypeTest {
    @Test
    public void integrityTest() {
        assertEquals("Enum does not contain the expected values", "[LIST, NUMBER, STRING]",
                Arrays.toString(VersionComponentType.values()));
    }
}
