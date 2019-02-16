package dev.aura.updatechecker.config;

import lombok.Getter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
  @Setting @Getter private General general = new General();

  @ConfigSerializable
  public static class General {
    @Setting(comment = "Enable debug logging")
    @Getter
    private boolean debug = false;

    @Setting(
      comment =
          "Select which language from the lang dir to use.\n"
              + "You can add your own translations in there. If you name your file \"test.lang\", choose \"test\" here."
    )
    @Getter
    private String language = "en_US";
  }
}
