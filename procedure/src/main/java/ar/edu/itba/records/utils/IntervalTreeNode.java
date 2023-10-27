package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Interval;

public class IntervalTreeNode {

    private IntervalTreeNode parent = null;
    private final Interval interval;

    public IntervalTreeNode(Interval interval) {
        this.interval = interval;
    }

    public IntervalTreeNode(Interval interval, IntervalTreeNode parent) {
        this.interval = interval;
        this.parent = parent;
    }

    public Interval getInterval() {
        return interval;
    }

    public IntervalTreeNode getParent() {
        return parent;
    }
}
