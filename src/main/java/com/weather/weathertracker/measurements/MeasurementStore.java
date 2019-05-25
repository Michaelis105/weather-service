package com.weather.weathertracker.measurements;

import java.time.ZonedDateTime;

public interface MeasurementStore {
  void add(Measurement measurement);

  Measurement fetch(ZonedDateTime timestamp);
}
