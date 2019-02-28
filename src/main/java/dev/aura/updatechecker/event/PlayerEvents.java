package dev.aura.updatechecker.event;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.permission.PermissionRegistry;
import java.util.concurrent.TimeUnit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.pagination.PaginationList;

public class PlayerEvents {
  @Listener(order = Order.POST)
  public void onPlayerJoin(ClientConnectionEvent.Join event) {
    final Player player = event.getTargetEntity();

    // Check before creating the task
    if ((AuraUpdateChecker.getVersionChecker().getUpdateMessagePagination() != null)
        && player.hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_JOIN)) {
      Sponge.getScheduler()
          .createTaskBuilder()
          .async()
          .delay(AuraUpdateChecker.getConfig().getTiming().getJoinMessageDelay(), TimeUnit.SECONDS)
          .execute(
              () -> {
                final PaginationList pagination =
                    AuraUpdateChecker.getVersionChecker().getUpdateMessagePagination();

                // Check again before actually executing, just to be sure the player can still
                // receive the message
                if (player.isOnline()
                    && (pagination != null)
                    && player.hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_JOIN)) {
                  pagination.sendTo(player);
                }
              })
          .submit(AuraUpdateChecker.getInstance());
    }
  }
}
