package dev.aura.updatechecker.checker;

import static org.junit.Assert.*;

import dev.aura.updatechecker.config.Config;
import dev.aura.updatechecker.permission.PermissionRegistry;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

public class VersionCheckerTest {
  @Test
  public void allErrorTest() {
    VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##"),
                new DummyPluginContainer("error!&!&&!&##")),
            Config.DEFAULT_CONFIG);

    assertEquals("Expected 3 errors", Optional.of(3), checker.checkForPluginAvailability());
  }

  @Test
  public void availabilityTest() {
    VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer("invsync"),
                new DummyPluginContainer("notavailablegfndfngkd"),
                new DummyPluginContainer("error!&!&&!&##")),
            Config.DEFAULT_CONFIG);

    assertEquals("Expected 1 error", Optional.of(1), checker.checkForPluginAvailability());
  }

  // TODO: Also check for console
  @Test
  public void canShowUpdateMessageTest() {
    final Player player = Mockito.mock(Player.class);
    final PaginationList pagination = Mockito.mock(PaginationList.class);

    final VersionChecker checker = new VersionChecker(null, null);

    Mockito.when(player.isOnline()).thenReturn(false, true);
    Mockito.when(player.hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_JOIN))
        .thenReturn(false, true);

    assertFalse(checker.canShowUpdateMessage(null));
    assertFalse(checker.canShowUpdateMessage(player));

    checker.updateMessagePagination = pagination;

    assertFalse(checker.canShowUpdateMessage(player));
    assertFalse(checker.canShowUpdateMessage(player));

    assertTrue(checker.canShowUpdateMessage(player));
  }

  // TODO: Also check for console
  @Test
  public void showUpdateMessageTest() {
    final Player player = Mockito.mock(Player.class);
    final PaginationList pagination = Mockito.mock(PaginationList.class);

    final VersionChecker checker = new VersionChecker(null, null);

    Mockito.when(player.isOnline()).thenReturn(false, true);
    Mockito.when(player.hasPermission(PermissionRegistry.NOTIFICATION_UPDATE_AVAIABLE_JOIN))
        .thenReturn(true);
    checker.updateMessagePagination = pagination;

    checker.showUpdateMessage(player);
    checker.showUpdateMessage(player);

    Mockito.verify(pagination, Mockito.times(1)).sendTo(player);
  }
}
