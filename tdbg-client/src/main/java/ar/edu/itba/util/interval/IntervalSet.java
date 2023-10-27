package ar.edu.itba.util.interval;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class IntervalSet {

    private final List<Interval> intervals;

    public static void main(String[] args) {
        IntervalSet is1 = new IntervalSet(IntervalParser.fromStringLimits("2012-01-04", "Now"));
        // IntervalSet is2 = is1.subtract(IntervalParser.fromStringLimits("2021-03-01", "2021-03-04"))
        //                     .subtract(IntervalParser.fromStringLimits("2015", "2016"))
        //                     .subtract(IntervalParser.fromStringLimits("2013-04-01 22:39", "2014-01-01 21:35"));
        System.out.println(is1);
    }

    public IntervalSet() {
        this.intervals = new LinkedList<>();
    }

    public IntervalSet(Interval interval) {
        this.intervals = Arrays.asList(interval);
    }

    public IntervalSet(Interval[] intervals) {
        this.intervals = Arrays.asList(intervals);
    }

    public IntervalSet(List<Interval> intervals) {
        this.intervals = intervals;
    }


    public List<Interval> getIntervals() {
        return intervals;
    }

    public IntervalSet intersection(IntervalSet other) {
        if (other == null) {
            return new IntervalSet(new LinkedList<>(intervals));
        }
        List<Interval> intersection = new LinkedList<>();
        intervals.forEach(
                interval -> other.intervals.forEach(
                        otherInterval -> interval.intersection(otherInterval)
                                                 .ifPresent(intersection::add)
                )
        );
        return new IntervalSet(intersection);
    }

    public IntervalSet intersection(Interval interval) {
        List<Interval> intersection = new LinkedList<>();
        intervals.forEach(
                thisInterval -> thisInterval.intersection(interval).ifPresent(i -> intersection.add((Interval) i))
        );
        return new IntervalSet(intersection);
    }

    public IntervalSet intersectAndReturnThisIntervals(IntervalSet other) {
        if (other == null) {
            return new IntervalSet(new LinkedList<>(intervals));
        }
        List<Interval> intersection = new LinkedList<>();
        intervals.forEach(
                interval -> other.intervals.forEach(
                        otherInterval -> interval.intersection(otherInterval)
                                .ifPresent(n -> intersection.add(interval))
                )
        );
        return new IntervalSet(intersection);
    }

    // TODO merge intervals to avoid repeating intersections
    public IntervalSet union(Interval interval) {
        if (this.intervals.size() == 0) return new IntervalSet(interval);
        List<Interval> newSet = new LinkedList<>(intervals);
        newSet.add(interval);
        return new IntervalSet(newSet);
    }

    public IntervalSet union(Interval interval, Long difference) {
        Interval lastInterval = intervals.get(intervals.size() - 1);
        if (interval.getStart().compareTo(lastInterval.getEnd()) > 0) {
            List<Interval> newSet = new LinkedList<>(intervals);
            newSet.add(interval);
            return new IntervalSet(newSet);
        }
        return null;
    }

    public Interval getLast() {
        return this.intervals.get(this.intervals.size() - 1);
    }

    public boolean isEmpty() {
        return this.intervals.size() == 0;
    }

    public IntervalSet subtract(Interval subtInterval) {
        LinkedList<Interval> newIntervals = new LinkedList<>();
        intervals.forEach(interval -> {
            newIntervals.addAll(interval.subtract(subtInterval));
        });
        // TODO check efficiency
        return new IntervalSet(newIntervals);
    }

    private int compareAux(IntervalSet otherSet, Comparator<Interval> comparator) {
        boolean isLesser;
        for (Interval interval: intervals) {
            isLesser = true;
            for (Interval otherInterval: otherSet.intervals) {
                if (comparator.compare(interval, otherInterval) >= 0) {
                    isLesser = false;
                    break;
                }
            }
            if (isLesser) {
                return -1;
            }
        }
        return 1;
    }

    public String toString() {
        if (intervals.size() == 0) return "[ ]";
        StringBuilder intervalSetString = new StringBuilder("[ ");
        for (int i = 0; i < intervals.size() -1; i++) {
            intervalSetString.append(intervals.get(i))
                .append(", ");
        }
        intervalSetString.append(intervals.get(intervals.size()-1))
            .append(" ]");
        return intervalSetString.toString();
    }
}
