package com.weather.weathertracker.statistics;

import java.time.ZonedDateTime;
import java.util.List;

import com.weather.weathertracker.measurements.Measurement;
import com.weather.weathertracker.measurements.MeasurementQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
public class StatsResource {

  @Autowired
  MeasurementQueryService queryService;

  @Autowired
  MeasurementAggregator aggregator;

  // orig code
  /*
  private final MeasurementQueryService queryService;
  private final MeasurementAggregator aggregator;

  public StatsResource(MeasurementQueryService queryService, MeasurementAggregator aggregator) {
    this.queryService = queryService;
    this.aggregator = aggregator;
  }
  */


  @GetMapping
  public List<AggregateResult> getStats(
    @RequestParam("metric") List<String> metrics,
    @RequestParam("stat") List<Statistic> stats,
    @RequestParam("fromDateTime") ZonedDateTime from,
    @RequestParam("toDateTime") ZonedDateTime to
    ) {
      List<Measurement> measurements = queryService.queryDateRange(from, to);
      return aggregator.analyze(measurements, metrics, stats);
  }
}
