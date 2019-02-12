package dev.aura.updatechecker.checker;

import lombok.Value;
import org.spongepowered.api.plugin.PluginContainer;

@Value
public class DummyPluginContainer implements PluginContainer {
  private String id;
}
