package dev.aura.updatechecker.event;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.permission.PermissionRegistry;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerEvents {
  @Listener
  public void onPlayerJoin(ClientConnectionEvent.Join event) {
    final Player player = event.getTargetEntity();

    if (player.hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_JOIN)) {
      AuraUpdateChecker.getVersionChecker().getUpdateMessagePagination().sendTo(player);
    }
  }
}
