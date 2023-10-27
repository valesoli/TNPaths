package ar.edu.itba.algorithms.utils.interval;

import java.util.Objects;

public class IntervalNodePair {

    private final Long node;
    private IntervalSet interval;

    public IntervalNodePair(Long node, IntervalSet interval) {
        this.node = node;
        this.interval = interval;
    }


    public Long getNode() {
        return node;
    }

    public IntervalSet getIntervalSet() {
        return interval;
    }

    public void setIntervalSet(IntervalSet interval) {
        this.interval = interval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.node, this.interval);
    }
}
