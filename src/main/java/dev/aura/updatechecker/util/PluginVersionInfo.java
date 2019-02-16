package dev.aura.updatechecker.util;

import com.google.common.collect.ImmutableMap;
import dev.aura.lib.version.Version;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Delegate;
import org.spongepowered.api.plugin.PluginContainer;

@Value
public class PluginVersionInfo {
  private final Version recommendedVersion;
  @EqualsAndHashCode.Exclude private final Version latestVersion;
  private final Version currentVersion;
  private final ImmutableMap<Date, Version> allVersions;

  @Delegate(excludes = Enum.class)
  @EqualsAndHashCode.Exclude
  private final PluginStatus pluginStatus;

  public PluginVersionInfo(
      Version recommendedVersion, Map<Date, Version> allVersions, PluginContainer plugin) {
    this(recommendedVersion, allVersions, new Version(plugin.getVersion().orElse("0.0.0")));
  }

  public PluginVersionInfo(
      Version recommendedVersion, Map<Date, Version> allVersions, Version currentVersion) {
    final SortedMap<Date, Version> allVersionsSorted = new TreeMap<>(Comparator.reverseOrder());
    allVersionsSorted.putAll(allVersions);

    this.recommendedVersion = recommendedVersion;
    this.latestVersion = allVersions.get(allVersionsSorted.firstKey());
    this.allVersions = ImmutableMap.copyOf(allVersionsSorted);
    this.currentVersion = currentVersion;

    pluginStatus =
        (currentVersion.compareTo(recommendedVersion) < 0)
            ? PluginStatus.NEW_RECOMMENDED
            : ((currentVersion.compareTo(latestVersion) < 0)
                ? PluginStatus.NEW_LATEST
                : PluginStatus.UP_TO_DATE);
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static enum PluginStatus {
    UP_TO_DATE(true),
    NEW_RECOMMENDED(false),
    NEW_LATEST(false);

    private final boolean upToDate;
  }
}
