package dev.aura.updatechecker.metrics;

import lombok.experimental.UtilityClass;
import org.bstats.sponge.Metrics2;

@UtilityClass
public class MetricManager {
  public static void startMetrics(Metrics2.Factory metricsFactory) {
    Metrics2 metrics = metricsFactory.make(4160);

    metrics.addCustomChart(new LanguageData());
  }
}
