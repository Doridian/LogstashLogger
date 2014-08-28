package com.foxelbox.foxellog.query;

public class AggregationResult {

    public final String label;
    public int placed = 0;
    public int destroyed = 0;

    public AggregationResult(String label) {
        this(label, 0, 0);
    }

    public AggregationResult(String label, int placed, int destroyed) {
        this.label = label;
        this.placed = placed;
        this.destroyed = destroyed;
    }

    @Override
    public String toString() {
        return "{" + label + "+" + placed + "-" + destroyed + "}";
    }
}
