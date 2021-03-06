package dev.aura.updatechecker.command;

import com.google.common.collect.ImmutableMap;
import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.message.PluginMessages;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandReload implements CommandExecutor {
  public static final String RELOAD_PERMISSION = CommandBase.BASE_PERMISSION + ".reload";

  private final AuraUpdateChecker plugin;

  public static CommandSpec create(AuraUpdateChecker plugin) {
    return CommandSpec.builder()
        .permission(RELOAD_PERMISSION)
        .description(Text.of("Reloads the plugin."))
        .executor(new CommandReload(plugin))
        .build();
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    Sponge.getScheduler()
        .createTaskBuilder()
        .async()
        .execute(
            () -> {
              try {
                plugin.reload(null);

                src.sendMessage(PluginMessages.ADMIN_RELOAD_SUCCESSFUL.getMessage());
              } catch (Exception e) {
                AuraUpdateChecker.getLogger().error("Error while reloading the plugin:", e);
                src.sendMessage(
                    PluginMessages.ADMIN_RELOAD_NOT_SUCCESSFUL.getMessage(
                        ImmutableMap.of("error", e.getMessage())));
              }
            })
        .submit(plugin);

    return CommandResult.success();
  }
}
