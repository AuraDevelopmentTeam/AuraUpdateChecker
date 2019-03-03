package dev.aura.updatechecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dev.aura.updatechecker.TestApi;
import dev.aura.updatechecker.permission.PermissionRegistry;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

public class VersionCheckerTest extends TestApi {
  @Test
  public void allErrorAvailabilitTest() {
    final VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer(ERROR_PROJECT1),
                new DummyPluginContainer(ERROR_PROJECT2),
                new DummyPluginContainer(ERROR_PROJECT3)),
            DEFAULT_CONFIG);

    assertEquals("Expected 3 errors", Optional.of(3), checker.checkForPluginAvailability());
  }

  @Test
  public void availabilityTest() {
    final VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer(PROJECT1),
                new DummyPluginContainer(MISSING_PROJECT1),
                new DummyPluginContainer(ERROR_PROJECT1)),
            DEFAULT_CONFIG);

    assertEquals("Expected 1 error", Optional.of(1), checker.checkForPluginAvailability());
    assertEquals("Expected 1 plugin to be available", 1, checker.checkablePlugins.size());
  }

  @Test
  public void versionTest() {
    final VersionChecker checker =
        new VersionChecker(
            Arrays.asList(
                new DummyPluginContainer(PROJECT1),
                new DummyPluginContainer(PROJECT2),
                new DummyPluginContainer(PROJECT3)),
            DEFAULT_CONFIG);

    // No errors here
    assertEquals("Expected 0 errors", Optional.of(0), checker.checkForPluginAvailability());

    // First check should change the data
    assertTrue(checker.checkForPluginUpdates());
    // Second shouldn't
    assertFalse(checker.checkForPluginUpdates());
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
