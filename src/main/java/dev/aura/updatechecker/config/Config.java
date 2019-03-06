package dev.aura.updatechecker.config;

import dev.aura.lib.messagestranslator.MessagesTranslator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {
  @Setting private General general = new General();
  @Setting private Timing timing = new Timing();

  @ConfigSerializable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class General {
    @Setting(comment = "Enable debug logging")
    private boolean debug = false;

    @Setting(
      comment =
          "Select which language from the lang dir to use.\n"
              + "You can add your own translations in there. If you name your file \"test.lang\", choose \"test\" here."
    )
    private String language = MessagesTranslator.DEFAULT_LANGUAGE;
  }

  @ConfigSerializable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
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
          "Specifies the interval (in minutes) in which admins will be reminded that there are plugin updates.\n"
              + "If the value is 0 or below, admins won't be reminded. Also no reminders of no updates."
    )
    private int remindAdminInterval = 30;

    @Setting(
      comment =
          "Specifies the interval (in minutes) in which the console will be reminded that there are plugin updates.\n"
              + "If the value is 0 or below, the console won't be reminded. Also no reminders of no updates."
    )
    private int remindConsoleInterval = 0;

    @Setting(
      comment =
          "Specifies the interval (in minutes) in which the plugin will check the OreAPI for plugin updates.\n"
              + "This is to to prevent spamming the API and still allowing for timely notifications when an update has been released. If\n"
              + "any new updates have been found during one of these updates, all admins will receive a notification."
    )
    private int updateVersionInfoInterval = 30;
  }
}
