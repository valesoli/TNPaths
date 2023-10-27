package ar.edu.itba.algorithms.operations;

import ar.edu.itba.algorithms.utils.interval.Interval;

public class Between {

    private final Interval interval;

    public Between(Interval interval) {
        this.interval = interval;
    }

    public boolean shouldApply(Interval otherInterval) {
        return otherInterval.isIntersecting(interval);
    }

}
