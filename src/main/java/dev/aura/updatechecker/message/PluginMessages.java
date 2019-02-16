package dev.aura.updatechecker.message;

import dev.aura.lib.messagestranslator.Message;
import dev.aura.updatechecker.AuraUpdateChecker;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@RequiredArgsConstructor
public enum PluginMessages implements Message {
  ;

  @Getter private final String stringPath;

  public Text getMessage() {
    return getMessage(null);
  }

  public Text getMessage(Map<String, String> replacements) {
    final String message =
        AuraUpdateChecker.getTranslator().translateWithFallback(this, replacements);

    return TextSerializers.FORMATTING_CODE.deserialize(message);
  }
}
