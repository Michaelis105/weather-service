Feature: Add a measurement
  In order to have source information to examine later
  I want to be able to capture a measurement of several metrics at a specific time

  Scenario: Add a measurement with valid (numeric) values
    # POST /measurements
    When I submit a new measurement as follows:
      | timestamp                  | temperature | dewPoint | precipitation |
      | "2015-09-01T16:00:00.000Z" | 27.1        | 16.7     | 0             |
    Then the response has a status code of 201
    And the Location header has the path "/measurements/2015-09-01T16:00:00.000Z"

  @new
  Scenario: Add a measurement while "failing" to report a metric
    # POST /measurements
    When I submit a new measurement as follows:
      | timestamp                  | temperature | dewPoint |
      | "2015-09-01T19:00:00.000Z" | 27.1        | 16.7     |
    Then the response has a status code of 201
    And the Location header has the path "/measurements/2015-09-01T19:00:00.000Z"

  Scenario: Cannot add a measurement with invalid values
    # POST /measurements
    When I submit a new measurement as follows:
      | timestamp                  | temperature    | dewPoint | precipitation |
      | "2015-09-01T16:00:00.000Z" | "not a number" | 16.7     | 0             |
    Then the response has a status code of 400

  Scenario: Cannot add a measurement without a timestamp
    # POST /measurements
    When I submit a new measurement as follows:
      | temperature | dewPoint | precipitation |
      | 27.1        | 20       | 0             |
    Then the response has a status code of 400

  @new
  @skip
  Scenario: Cannot add a measurement without a datapoint in metric
    # POST /measurements
    When I submit a new measurement as follows:
      | timestamp                  | temperature | dewPoint | precipitation |
      | "2015-09-01T16:00:00.000Z" | 27.1        | 20       | ""            |
    Then the response has a status code of 400

  @new
  Scenario: Cannot add a measurement without a timestamp null empty
  # POST /measurements
    When I submit a new measurement as follows:
      | timestamp | temperature | dewPoint | precipitation |
      | ""        | 27.1        | 16.7     | 0             |
    Then the response has a status code of 400

  @new
  Scenario: Add a measurement with valid (numeric) values but with different timezone
    # POST /measurements
    When I submit a new measurement as follows:
      | timestamp                                  | temperature | dewPoint | precipitation |
      | "2015-09-01T17:00:00.000+01:00[UTC+01:00]" | 27.1        | 16.7     | 0             |
    Then the response has a status code of 201
    And the Location header has the path "/measurements/2015-09-01T16:00:00.000Z"

