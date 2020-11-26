package dev.aura.updatechecker.util;

import com.google.common.collect.ImmutableMap;
import dev.aura.lib.version.Version;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Delegate;
import org.spongepowered.api.plugin.PluginContainer;

@Value
public class PluginVersionInfo {
  private final Version installedVersion;
  private final Version recommendedVersion;
  @EqualsAndHashCode.Exclude private final Version latestVersion;
  private final ImmutableMap<Instant, Version> allVersions;

  @Delegate(excludes = Enum.class)
  @EqualsAndHashCode.Exclude
  private final PluginStatus pluginStatus;

  public PluginVersionInfo(
      PluginContainer plugin, Version recommendedVersion, Map<Instant, Version> allVersions) {
    this(new Version(plugin.getVersion().orElse("0.0.0")), recommendedVersion, allVersions);
  }

  public PluginVersionInfo(
      Version installedVersion, Version recommendedVersion, Map<Instant, Version> allVersions) {
    final SortedMap<Instant, Version> allVersionsSorted = new TreeMap<>(Comparator.reverseOrder());
    allVersionsSorted.putAll(allVersions);

    this.recommendedVersion = recommendedVersion;
    this.latestVersion = allVersions.get(allVersionsSorted.firstKey());
    this.allVersions = ImmutableMap.copyOf(allVersionsSorted);
    this.installedVersion = installedVersion;

    pluginStatus =
        (installedVersion.compareTo(recommendedVersion) < 0)
            ? ((recommendedVersion.compareTo(latestVersion) < 0)
                ? PluginStatus.NEW_BOTH
                : PluginStatus.NEW_RECOMMENDED)
            : ((installedVersion.compareTo(latestVersion) < 0)
                ? PluginStatus.NEW_LATEST
                : PluginStatus.UP_TO_DATE);
  }

  @Getter
  @RequiredArgsConstructor
  @ToString
  public static enum PluginStatus {
    UP_TO_DATE(true, false, false),
    NEW_RECOMMENDED(false, true, false),
    NEW_LATEST(false, false, true),
    NEW_BOTH(false, true, true);

    private final boolean upToDate;
    private final boolean newRecommended;
    private final boolean newLatest;
  }
}
