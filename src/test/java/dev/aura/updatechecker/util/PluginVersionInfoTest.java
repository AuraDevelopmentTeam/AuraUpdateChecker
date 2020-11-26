package dev.aura.updatechecker.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableMap;
import dev.aura.lib.version.Version;
import dev.aura.updatechecker.checker.DummyPluginContainer;
import java.time.Instant;
import org.junit.Test;

public class PluginVersionInfoTest {
  private static final String DUMMY_VERSION_STRING = "1.2.3";
  private static final Version EMPTY_VERSION = new Version("");
  private static final Version DUMMY_VERSION = new Version(DUMMY_VERSION_STRING);

  private static void verifyPluginStatus(
      PluginVersionInfo versionInfo, PluginVersionInfo.PluginStatus pluginStatus) {
    assertEquals(pluginStatus, versionInfo.getPluginStatus());
    assertEquals(pluginStatus.isUpToDate(), versionInfo.isUpToDate());
    assertEquals(pluginStatus.isNewRecommended(), versionInfo.isNewRecommended());
    assertEquals(pluginStatus.isNewLatest(), versionInfo.isNewLatest());
  }

  @Test
  public void pluginContainerTest() {
    final PluginVersionInfo versionInfo =
        new PluginVersionInfo(
            new DummyPluginContainer("dummy", DUMMY_VERSION_STRING),
            EMPTY_VERSION,
            ImmutableMap.of(Instant.EPOCH, EMPTY_VERSION));
    final PluginVersionInfo versionInfoEmpty =
        new PluginVersionInfo(
            new DummyPluginContainer("dummy"),
            EMPTY_VERSION,
            ImmutableMap.of(Instant.EPOCH, EMPTY_VERSION));

    assertEquals(DUMMY_VERSION, versionInfo.getInstalledVersion());
    assertEquals(new Version("0.0.0"), versionInfoEmpty.getInstalledVersion());
  }

  @Test
  public void getLatestVersionTest() {
    final Version latestVersion = DUMMY_VERSION;

    final PluginVersionInfo versionInfo =
        new PluginVersionInfo(
            EMPTY_VERSION,
            EMPTY_VERSION,
            ImmutableMap.of(
                Instant.ofEpochSecond(5),
                new Version("0.1.1"),
                Instant.ofEpochSecond(10),
                latestVersion,
                Instant.ofEpochSecond(0),
                new Version("2.4.6")));

    assertSame(latestVersion, versionInfo.getLatestVersion());
    assertEquals(new Version(DUMMY_VERSION_STRING), versionInfo.getLatestVersion());
  }

  @Test
  public void pluginStatusTest() {
    final Version oldVersion = new Version("1.2.2");
    final Version installedVersion = DUMMY_VERSION;
    final Version newVersion = new Version("1.2.4");
    final Version superNewVersion = new Version("1.2.5");

    final Instant instant = Instant.EPOCH;

    final PluginVersionInfo upToDateSame =
        new PluginVersionInfo(
            installedVersion, installedVersion, ImmutableMap.of(instant, installedVersion));
    final PluginVersionInfo upToDateOld =
        new PluginVersionInfo(installedVersion, oldVersion, ImmutableMap.of(instant, oldVersion));
    final PluginVersionInfo newRecommendedOldLatest =
        new PluginVersionInfo(installedVersion, newVersion, ImmutableMap.of(instant, oldVersion));
    final PluginVersionInfo newRecommendedNewLatest =
        new PluginVersionInfo(installedVersion, newVersion, ImmutableMap.of(instant, newVersion));
    final PluginVersionInfo newLatest =
        new PluginVersionInfo(installedVersion, oldVersion, ImmutableMap.of(instant, newVersion));
    final PluginVersionInfo newBoth =
        new PluginVersionInfo(
            installedVersion, newVersion, ImmutableMap.of(instant, superNewVersion));

    verifyPluginStatus(upToDateSame, PluginVersionInfo.PluginStatus.UP_TO_DATE);
    verifyPluginStatus(upToDateOld, PluginVersionInfo.PluginStatus.UP_TO_DATE);
    verifyPluginStatus(newRecommendedOldLatest, PluginVersionInfo.PluginStatus.NEW_RECOMMENDED);
    verifyPluginStatus(newRecommendedNewLatest, PluginVersionInfo.PluginStatus.NEW_RECOMMENDED);
    verifyPluginStatus(newLatest, PluginVersionInfo.PluginStatus.NEW_LATEST);
    verifyPluginStatus(newBoth, PluginVersionInfo.PluginStatus.NEW_BOTH);
  }
}
