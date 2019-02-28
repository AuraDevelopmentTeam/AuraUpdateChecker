package dev.aura.updatechecker.event;

import dev.aura.updatechecker.AuraUpdateChecker;
import dev.aura.updatechecker.checker.VersionChecker;
import java.util.concurrent.TimeUnit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerEvents {
  @Listener(order = Order.POST)
  public void onPlayerJoin(ClientConnectionEvent.Join event) {
    final Player player = event.getTargetEntity();
    final VersionChecker checker = AuraUpdateChecker.getVersionChecker();

    // Check before creating the task
    if (checker.canShowUpdateMessage(player)) {
      Sponge.getScheduler()
          .createTaskBuilder()
          .async()
          .delay(AuraUpdateChecker.getConfig().getTiming().getJoinMessageDelay(), TimeUnit.SECONDS)
          .execute(() -> checker.showUpdateMessage(player))
          .submit(AuraUpdateChecker.getInstance());
    }
  }
}
