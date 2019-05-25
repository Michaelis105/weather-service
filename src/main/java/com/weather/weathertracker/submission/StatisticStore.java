package com.weather.weathertracker.submission;

/**
 * Quick and dirty way to store statstics to a metric name.
 */
public class StatisticStore {

    private Double min;
    private Double max;
    private Double sum;
    private Integer count;

    public StatisticStore() {

        // These would be ideally immediately overwritten.
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;

        sum = 0.0;
        count = 0;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
