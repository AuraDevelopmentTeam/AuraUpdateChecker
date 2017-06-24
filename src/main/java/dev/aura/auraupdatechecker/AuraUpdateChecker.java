package dev.aura.auraupdatechecker;

import org.bstats.MetricsLite;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

@Plugin(id = AuraUpdateChecker.ID, name = AuraUpdateChecker.NAME, version = AuraUpdateChecker.VERSION, description = AuraUpdateChecker.DESCRIPTION, authors = {
        AuraUpdateChecker.AUTHOR_BRAINSTONE })
public class AuraUpdateChecker {
    public static final String ID = "@id@";
    public static final String NAME = "@name@";
    public static final String VERSION = "@version@";
    public static final String DESCRIPTION = "@description@";
    public static final String AUTHOR_BRAINSTONE = "BrainStone";

    @Inject
    protected MetricsLite metrics;

    @Listener
    public void init(GameInitializationEvent init) {
        ;
    }

    @Listener
    public void loadComplete(GameLoadCompleteEvent loadComplete) {
        ;
    }
}
