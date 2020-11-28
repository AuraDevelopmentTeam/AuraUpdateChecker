package dev.aura.updatechecker;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.core.config.LogLevel;
import io.specto.hoverfly.junit.core.model.JournalEntry;
import io.specto.hoverfly.junit.core.model.Response;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import io.specto.hoverfly.junit.verification.HoverflyVerificationError;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import org.junit.Before;
import org.junit.ClassRule;

public class TestApi extends TestBase {
  @ClassRule
  public static HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(
          SimulationSource.defaultPath("simulation.json"),
          HoverflyConfig.localConfigs().logLevel(LogLevel.DEBUG));

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

          final String totalCounts =
              "\n\nTotal Counts:\n"
                  + Joiner.on("\n").withKeyValueSeparator(": ").join(statusCodeCounts);

          if (statusCodeCounts.get(HttpsURLConnection.HTTP_OK) != expectedHTTP_OK) {
            throw new HoverflyVerificationError(
                "Expected "
                    + expectedHTTP_OK
                    + " times HTTP OK but got "
                    + statusCodeCounts.get(HttpsURLConnection.HTTP_OK)
                    + '.'
                    + totalCounts);
          } else if (statusCodeCounts.get(HttpsURLConnection.HTTP_NOT_FOUND)
              != expectedHTTP_NOT_FOUND) {
            throw new HoverflyVerificationError(
                "Expected "
                    + expectedHTTP_NOT_FOUND
                    + " times HTTP NOT_FOUND but got "
                    + statusCodeCounts.get(HttpsURLConnection.HTTP_NOT_FOUND)
                    + '.'
                    + totalCounts);
          } else if (statusCodeCounts.size() != 2) {
            throw new HoverflyVerificationError(
                "Expected to see only HTTP OK and HTTP NOT_FOUND." + totalCounts);
          }
        });
  }

  @Before
  public void resetJournal() {
    hoverflyRule.resetJournal();
  }
}
