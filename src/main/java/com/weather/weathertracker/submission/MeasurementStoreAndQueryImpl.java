package com.weather.weathertracker.submission;

import com.weather.weathertracker.measurements.Measurement;
import com.weather.weathertracker.measurements.MeasurementQueryService;
import com.weather.weathertracker.measurements.MeasurementStore;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
/**
 * MeasurementStore and MeasurementQueryService are merged as a single implementation.
 * MeasurementQueryService should have ideally been implemented as a separate service
 * but it requires access to the store.
 *
 * In other words, it is better for MeasurementQueryService to truncate queryDateRange
 * having access to all datapoints instead of inefficiently "guessing" which measurements are within the range.
 */
public class MeasurementStoreAndQueryImpl implements MeasurementStore, MeasurementQueryService {

    /*
        While it would be reasonable that all devices pushing data will have sync'd ZonedDateTime, it is not guaranteed
        that insertion order will be ordered.

        For example, Dev A pushing data at 1239.000 might reach the service AFTER Dev B pushing data at 1239.001.

        Since all data is stored in memory, pushing big data, frequently, without flushing to long-term,
        or garbage collection will lead to inevitable running out of memory.

        Maybe replace with Spring Repository time permitting.
      */

    Map<ZonedDateTime, Measurement> zoneToMeasurementMap = new HashMap<>();

    @Override
    public void add(Measurement measurement) {

        // Basic checks.
        if (measurement == null) {
            throw new IllegalArgumentException("Cannot add null measurement.");
        } else if (measurement.getTimestamp() == null) {
            throw new IllegalArgumentException("Cannot add measurement without timestamp");
            // Or perhaps default to using the timestamp at time of add attempt?
        } else if (measurement.getMetrics() == null || measurement.getMetrics().size() == 0) {
            throw new IllegalArgumentException("Measurements has no metrics to add?");
            // Or perhaps device is operational but did not report any metrics? This could be valid workflow.
        } else if (zoneToMeasurementMap.get(measurement.getTimestamp()) != null) {
            throw new IllegalArgumentException("Measurement with same timestamp already exists.");
            // Don't overwrite measurement for now, would there be a specific, applicable workflow for this?
            // This would break fetch since it is possible that two measurements occur at the EXACT same nanosecond.
        }

        for (Map.Entry<String, Double> metric : measurement.getMetrics().entrySet()) {
            if (StringUtils.isEmpty(metric.getKey())) {
                throw new IllegalArgumentException("Cannot add measurement with a null/empty metric.");
            } else if (Double.isNaN(metric.getValue())) {
                throw new IllegalArgumentException("Cannot add measurement with an invalid statistic.");
            }
        }

        // Timestamp could be from different time zone, standardize with UTC.
        ZonedDateTime timestamp = measurement.getTimestamp().withZoneSameInstant(ZoneId.of("UTC"));

        zoneToMeasurementMap.put(timestamp, measurement);
    }

    @Override
    public Measurement fetch(ZonedDateTime timestamp) {

        // Basic checks.
        if (null == timestamp) {
            System.out.println("Cannot query with null zoned date time.");
            return null;
        }

        // Timestamp could be from different time zone, standardize with UTC.
        // Input timestamp is always offset Z and zone Z for some reason :(.
        ZonedDateTime timestampConverted = timestamp.withZoneSameInstant(ZoneId.of("UTC"));

        /*
        // Maybe uncomment this if test case is applicable.
        if (zoneToMeasurementMap.isEmpty()) {
            System.out.println("There are no measurements in store to fetch from.");
            return null;
        }
        */

        return zoneToMeasurementMap.get(timestampConverted);
    }

    // TODO: Maybe use caching here except any new data inserted within range invalidates results.
    @Override
    public List<Measurement> queryDateRange(ZonedDateTime from, ZonedDateTime to) {

        if (from == null || to == null) {
            System.out.println("Cannot query with null zoned date time.");
            return null;
        }

        // timestamp is always offset Z and zone Z, force UTC
        from = from.withZoneSameInstant(ZoneId.of("UTC"));
        to = to.withZoneSameInstant(ZoneId.of("UTC"));

        List<Measurement> measurements = new LinkedList<>(zoneToMeasurementMap.values());

        // TODO: Do some optimization here (non-overlapping queries or easier truncating on ordered ZonedDateTime elements).

        // Truncate all measurements with zoned date time between `before` inclusive and `to` exclusive.
        int i = 0;
        while(i < measurements.size()) {
            ZonedDateTime zonedDateTime = measurements.get(i).getTimestamp();
            if (!(isAfterInclusive(zonedDateTime, from) && (zonedDateTime.isBefore(to)))) {
                measurements.remove(i--);
            }
            i++;
        }

        return measurements;
    }

    // Adapted from ChronoZonedDateTime.java since native libraries are range exclusive.
    private boolean isAfterInclusive(ZonedDateTime a, ZonedDateTime b) {
        long thisEpochSec = a.toEpochSecond();
        long otherEpochSec = b.toEpochSecond();
        return thisEpochSec >= otherEpochSec || (thisEpochSec == otherEpochSec && a.toLocalTime().getNano() >= b.toLocalTime().getNano());
    }
}
