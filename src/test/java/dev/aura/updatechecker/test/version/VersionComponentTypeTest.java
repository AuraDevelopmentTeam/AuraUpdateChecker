package dev.aura.updatechecker.test.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

import dev.aura.updatechecker.version.VersionComponentType;

public class VersionComponentTypeTest {
    @Test
    public void integrityTest() {
        assertEquals("Enum does not contain the expected values", "[LIST, NUMBER, STRING]",
                Arrays.toString(VersionComponentType.values()));

        for (VersionComponentType type : VersionComponentType.values()) {
            assertSame("Turning the type into a string and back doesn't work!", type,
                    VersionComponentType.valueOf(type.toString()));
        }
    }
}
