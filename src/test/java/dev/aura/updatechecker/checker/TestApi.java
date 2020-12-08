package dev.aura.updatechecker.checker;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import dev.aura.updatechecker.TestBase;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.core.config.LogLevel;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.core.model.Response;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.junit.verification.HoverflyVerificationError;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.ClassRule;

@SuppressFBWarnings(
    value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
    justification = "If getParent() returns null here (it's a file!) we have bigger issues")
public class TestApi extends TestBase {
  private static final String simulationFile = "simulation.json";

  @ClassRule
  public static final HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(
          SimulationSource.defaultPath(simulationFile),
          HoverflyConfig.localConfigs()
              .logLevel(LogLevel.DEBUG)
              .addCommands(
                  "-response-body-files-path",
                  getPathOfResource(simulationFile).getParent().toAbsolutePath().toString()));

  protected static final String PROJECT1 = "project1";
  protected static final String PROJECT2 = "project2";
  protected static final String PROJECT3 = "project3";
  protected static final ImmutableList<String> PROJECTS =
      ImmutableList.of(PROJECT1, PROJECT2, PROJECT3);

  protected static final String MISSING_PROJECT1 = "missing_project1";
  protected static final String MISSING_PROJECT2 = "missing_project2";
  protected static final String MISSING_PROJECT3 = "missing_project3";
  protected static final ImmutableList<String> MISSING_PROJECTS =
      ImmutableList.of(MISSING_PROJECT1, MISSING_PROJECT2, MISSING_PROJECT3);

  protected static final String ERROR_PROJECT1 = "spaces are bad";
  protected static final String ERROR_PROJECT2 = "!!7678&%/(!&7867(%!&%78'#235#2?";
  protected static final String ERROR_PROJECT3 = "error!&!&&!&##";
  protected static final ImmutableList<String> ERROR_PROJECTS =
      ImmutableList.of(ERROR_PROJECT1, ERROR_PROJECT2, ERROR_PROJECT3);

  private static URL findResourceOnClasspath(String resourceName) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(classLoader.getResource(resourceName))
        .orElseThrow(
            () -> new IllegalArgumentException("Resource not found with name: " + resourceName));
  }

  @SneakyThrows(URISyntaxException.class)
  private static Path getPathOfResource(String resource) {
    return Paths.get(
        findResourceOnClasspath(HoverflyConstants.DEFAULT_HOVERFLY_RESOURCE_DIR + '/' + resource)
            .toURI());
  }

  protected static void assertRequestCountMatch(
      final long expectedHTTP_OK, final long expectedHTTP_NOT_FOUND) {
    hoverflyRule.verify(
        service(any()).anyMethod(any()).anyBody().anyQueryParams(),
        (request, data) -> {
          Map<Integer, Long> statusCodeCounts =
              data.getJournal().getEntries().stream()
                  .map(JournalEntry::getResponse)
                  .map(Response::getStatus)
                  .collect(
                      Collectors.groupingBy(
                          Function.identity(), TreeMap::new, Collectors.counting()));

          statusCodeCounts.putIfAbsent(HttpsURLConnection.HTTP_OK, 0L);
          statusCodeCounts.putIfAbsent(HttpsURLConnection.HTTP_NOT_FOUND, 0L);

          final Supplier<String> totalCounts =
              () ->
                  ("\n\nTotal Counts:\n"
                      + Joiner.on("\n").withKeyValueSeparator(": ").join(statusCodeCounts));

          if (statusCodeCounts.get(HttpsURLConnection.HTTP_OK) != expectedHTTP_OK) {
            throw new HoverflyVerificationError(
                "Expected "
                    + expectedHTTP_OK
                    + " times HTTP OK but got "
                    + statusCodeCounts.get(HttpsURLConnection.HTTP_OK)
                    + '.'
                    + totalCounts.get());
          } else if (statusCodeCounts.get(HttpsURLConnection.HTTP_NOT_FOUND)
              != expectedHTTP_NOT_FOUND) {
            throw new HoverflyVerificationError(
                "Expected "
                    + expectedHTTP_NOT_FOUND
                    + " times HTTP NOT_FOUND but got "
                    + statusCodeCounts.get(HttpsURLConnection.HTTP_NOT_FOUND)
                    + '.'
                    + totalCounts.get());
          } else if (statusCodeCounts.size() != 2) {
            throw new HoverflyVerificationError(
                "Expected to see only HTTP OK and HTTP NOT_FOUND." + totalCounts.get());
          }
        });
  }

  @Before
  public void resetJournal() {
    hoverflyRule.resetJournal();
  }

  @SuppressFBWarnings(
      value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
      justification =
          "Not an issue as if there actually are parallelization issues it'll crash elsewhere.")
  @Before
  public void resetCounter() {
    OreAPI.resetErrorCounter();
    OreAPI.authHeader = null;
  }
}
