package dev.aura.updatechecker.command;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.checker.VersionChecker;
import dev.aura.updatechecker.message.PluginMessages;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandShow implements CommandExecutor {
  public static final String SHOW_PERMISSION = CommandBase.BASE_PERMISSION + ".show";

  private final VersionChecker checker;

  public static CommandSpec create() {
    return CommandSpec.builder()
        .permission(SHOW_PERMISSION)
        .description(Text.of("Show the available updates again."))
        .executor(new CommandShow(AuraUpdateChecker.getVersionChecker()))
        .build();
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (checker.canShowUpdateMessage(src)) {
      checker.showUpdateMessage(src);
    } else {
      src.sendMessage(PluginMessages.NOTIFICATION_NO_UPDATE_AVAILABLE.getMessage());
    }

    return CommandResult.success();
  }
}
