package com.weather.weathertracker.statistics;

import java.util.List;

import com.weather.weathertracker.measurements.Measurement;

public interface MeasurementAggregator {
  List<AggregateResult> analyze(List<Measurement> measurements, List<String> metrics, List<Statistic> stats);
}
