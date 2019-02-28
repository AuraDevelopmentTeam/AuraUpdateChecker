package dev.aura.updatechecker.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
@Data
@Setter(AccessLevel.NONE)
public class Config {
  @Setting private General general = new General();
  @Setting private Timing timing = new Timing();

  @ConfigSerializable
  @Data
  @Setter(AccessLevel.NONE)
  public static class General {
    @Setting(comment = "Enable debug logging")
    private boolean debug = false;

    @Setting(
      comment =
          "Select which language from the lang dir to use.\n"
              + "You can add your own translations in there. If you name your file \"test.lang\", choose \"test\" here."
    )
    private String language = "en_US";
  }

  @ConfigSerializable
  @Data
  @Setter(AccessLevel.NONE)
  public static class Timing {
    @Setting(
      comment =
          "Specifies the connection timeout (in milliseconds) for all connections to the OreAPI.\n"
              + "If you get timeouts from this plugin, increase this value."
    )
    private int connectionTimeout = 500;

    @Setting(
      comment =
          "Specifies the time (in seconds) to wait to show the available updates message after an admin joined.\n"
              + "If the message is hidden by other plugins/messages, increase this value."
    )
    private int joinMessageDelay = 5;

    @Setting(
      comment =
          "Specifies the interval (in minutes) in which the plugin will check the OreAPI for plugin updates.\n"
              + "This is to to prevent spamming the API and still allowing for timely notifications when an update has been released. If\n"
              + "any new updates have been found during one of these updates, all admins will receive a notification."
    )
    private int updateVersionInfoInterval = 30;
  }
}
