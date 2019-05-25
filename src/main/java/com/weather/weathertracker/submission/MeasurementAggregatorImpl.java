package com.weather.weathertracker.submission;

import com.weather.weathertracker.measurements.Measurement;
import com.weather.weathertracker.statistics.AggregateResult;
import com.weather.weathertracker.statistics.MeasurementAggregator;
import com.weather.weathertracker.statistics.Statistic;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MeasurementAggregatorImpl implements MeasurementAggregator {

    @Override
    public List<AggregateResult> analyze(List<Measurement> measurements, List<String> metrics, List<Statistic> stats) {

        // Basic checks
        if (measurements == null || measurements.isEmpty()) {
            System.out.println("Cannot analyze null or empty list of measurements.");
            return null;
        } else if (metrics == null || metrics.isEmpty()) {
            System.out.println("Cannot analyze null or empty list of metrics.");
            return null;
        } else if (stats == null || stats.isEmpty()) {
            System.out.println("Cannot analyze null or empty list of statistics.");
            return null;
        }

        // Filter out all nulls or empty strings.
        measurements.removeAll(Collections.singleton(null));
        metrics.removeAll(Collections.singleton(null));
        metrics.removeAll(Collections.singleton(""));
        stats.removeAll(Collections.singleton(null));

        // TODO: How to handle duplicate metrics and statistics???

        // For purposes of this test and time constraints, it is simpler to calculate all statistics and then retrieve
        // based on what is queried than to dynamically parse the statistic and calculate that particular statistic
        // "on the go".

        // This is assuming no other statistic will be added in the future such standard deviation, variation,
        // and other math term.

        /*
            MetricName->StatisticStore:
                        - Min->Double
                        - Max->Double
                        - Sum->Double
                        - Count->Integer
         */

        Map<String, StatisticStore> metricStatMap = new HashMap<>();

        // Calculate the min, max, and average of all measurements.
        // This does not scale well for large measurement sets.
        for (Measurement measurement : measurements) {
            for (Map.Entry<String, Double> metric : measurement.getMetrics().entrySet()) {
                String metricName = metric.getKey();

                if (!metricStatMap.containsKey(metricName)) {
                    metricStatMap.put(metricName, new StatisticStore());
                }

                StatisticStore statisticStore = metricStatMap.get(metricName);
                statisticStore.setMin(Math.min(metric.getValue(), statisticStore.getMin()));
                statisticStore.setMax(Math.max(metric.getValue(), statisticStore.getMax()));

                // Running average.
                // Use BigDecimal to avoid inherent precision issues.
                statisticStore.setSum(BigDecimal.valueOf(statisticStore.getSum()).add(BigDecimal.valueOf(metric.getValue())).doubleValue());
                statisticStore.setCount(statisticStore.getCount()+1);
            }
        }

        List<AggregateResult> results = new LinkedList<>();

        // At this point, all calculations done whether used or not.
        // Retrieve statistics by metrics.
        for (String metric : metrics) {
            for (Statistic statistic : stats) {
                Double value = 0.0;
                if (metricStatMap.containsKey(metric)) {
                    StatisticStore statisticStore = metricStatMap.get(metric);
                    switch(statistic) {
                        case AVERAGE:
                            // Use BigDecimal to avoid inherent precision issues.
                            value = BigDecimal.valueOf(statisticStore.getSum()).divide(BigDecimal.valueOf(statisticStore.getCount())).doubleValue();
                            break;
                        case MIN:
                            value = statisticStore.getMin();
                            break;
                        case MAX:
                            value = statisticStore.getMax();
                            break;
                        default:
                            System.out.println("Unknown statistic: " + statistic);
                    }
                    results.add(new AggregateResult(metric, statistic, value));
                }
            }
        }

        return results;
    }
}
