package dev.aura.auraupdatechecker.checker;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

import dev.aura.auraupdatechecker.AuraUpdateChecker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VersionChecker {
    private final Collection<PluginContainer> availablePlugins;
    private List<PluginContainer> checkablePlugins;

    public void checkForPluginAvailability() {
        final Logger logger = AuraUpdateChecker.getLogger();

        if (checkablePlugins != null) {
            logger.info("Already checked plugins for availability!");

            return;
        }

        checkablePlugins = availablePlugins.stream().filter(plugin -> {
            boolean isOnOre = OreAPI.isOnOre(plugin);

            if (isOnOre) {
                logger.debug("Plugin " + plugin.getName() + " is available on Ore Repository.");
            }

            return isOnOre;
        }).collect(Collectors.toList());
    }
}
