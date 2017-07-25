package dev.aura.updatechecker.test.checker;

import org.spongepowered.api.plugin.PluginContainer;

import lombok.Value;

@Value
public class DummyPluginContainer implements PluginContainer {
    private String id;
}
