package dev.aura.updatechecker.command;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.permission.PermissionRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandBase {
  public static final String BASE_PERMISSION = PermissionRegistry.COMMAND;

  public static void register(AuraUpdateChecker plugin) {
    CommandSpec updatechecker =
        CommandSpec.builder()
            .description(Text.of("Enables or disables synchronizing the world time with realtime."))
            .child(CommandReload.create(plugin), "reload", "r", "rl", "re", "rel")
            .build();

    Sponge.getCommandManager()
        .register(plugin, updatechecker, "updatechecker", "updatecheck", "uc", "upcheck", "up");
  }
}
