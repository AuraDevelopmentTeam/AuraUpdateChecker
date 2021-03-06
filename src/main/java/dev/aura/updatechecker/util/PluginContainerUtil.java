package dev.aura.updatechecker.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.plugin.PluginContainer;

@UtilityClass
public class PluginContainerUtil {
  private static final Pattern ID_PATTERN = Pattern.compile("<pluginId>");
  private static final Function<PluginContainer, String> ID_PROVIDER = PluginContainer::getId;
  private static final Pattern NAME_PATTERN = Pattern.compile("<pluginNAME>");
  private static final Function<PluginContainer, String> NAME_PROVIDER = PluginContainer::getName;
  private static final Map<Pattern, Function<PluginContainer, String>> placeHolders =
      new HashMap<>();

  public static String getPluginString(PluginContainer plugin) {
    return (plugin == null) ? "<none>" : ('"' + plugin.getName() + "\" (" + plugin.getId() + ')');
  }

  public static String replacePluginPlaceHolders(String template, PluginContainer plugin) {
    Matcher match;
    String result = template;

    for (Entry<Pattern, Function<PluginContainer, String>> placeHolder : placeHolders.entrySet()) {
      match = placeHolder.getKey().matcher(result);

      if (match.find()) {
        result = match.replaceAll(placeHolder.getValue().apply(plugin));
      }
    }

    return result;
  }

  static {
    placeHolders.put(ID_PATTERN, ID_PROVIDER);
    placeHolders.put(NAME_PATTERN, NAME_PROVIDER);
  }
}
