package dev.aura.updatechecker.util;

import com.google.common.collect.ImmutableMap;
import dev.aura.lib.version.Version;
import java.util.Date;
import java.util.SortedMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PluginVersionInfo {
  private final Version recommendedVersion;
  @EqualsAndHashCode.Exclude private final Version latestVersion;
  private final ImmutableMap<Date, Version> allVersions;

  public PluginVersionInfo(Version recommendedVersion, SortedMap<Date, Version> allVersions) {
    this(
        recommendedVersion,
        allVersions.get(allVersions.firstKey()),
        ImmutableMap.copyOf(allVersions));
  }
}
