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
            .description(Text.of("Base command for the plugin. Does nothing on its own."))
            .child(CommandReload.create(plugin), "reload", "r", "rl", "re", "rel")
            .child(CommandShow.create(), "show", "s", "sh", "view", "v")
            .build();

    Sponge.getCommandManager()
        .register(
            plugin, updatechecker, AuraUpdateChecker.ID, "updatecheck", "uc", "upcheck", "up");
  }
}
