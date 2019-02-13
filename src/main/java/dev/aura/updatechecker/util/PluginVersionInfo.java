package dev.aura.updatechecker.util;

import com.google.common.collect.ImmutableMap;
import dev.aura.lib.version.Version;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
public class PluginVersionInfo {
  private final Version recommendedVersion;
  @EqualsAndHashCode.Exclude private final Version latestVersion;
  private final ImmutableMap<Date, Version> allVersions;

  public PluginVersionInfo(Version recommendedVersion, Map<Date, Version> allVersions) {
    final SortedMap<Date, Version> allVersionsSorted = new TreeMap<>(Comparator.reverseOrder());
    allVersionsSorted.putAll(allVersions);

    this.recommendedVersion = recommendedVersion;
    this.latestVersion = allVersions.get(allVersionsSorted.firstKey());
    this.allVersions = ImmutableMap.copyOf(allVersionsSorted);
  }
}
