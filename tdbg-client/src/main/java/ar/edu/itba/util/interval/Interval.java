package ar.edu.itba.util.interval;

import ar.edu.itba.util.Utils;
import ar.edu.itba.util.time.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Interval {

    private final Long start;
    private final Long end; // 1998 -> X
    private final Granularity granularity;
    private boolean isNow = false;
    public static final Long MAX_SECONDS_FROM_EPOCH = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public Interval(Long start, Long end, Granularity granularity) {
        this.start = start;
        this.end = end;
        this.granularity = granularity;
        this.isNow = end >= Interval.MAX_SECONDS_FROM_EPOCH;
        this.checkTime();
    }

    public Interval(Long start, Long end, Granularity granularity, boolean isNow) {
        this(start, end, granularity);
        this.isNow = isNow;
    }

    public Interval(DateTime start, DateTime end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(true);
        this.granularity = Granularity.DATETIME;
        this.checkTime();
    }

    public Interval(Date start, Date end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(true);
        this.granularity = Granularity.DATE;
        this.checkTime();
    }

    public Interval(YearMonth start, YearMonth end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(true);
        this.granularity = Granularity.YEAR_MONTH;
        this.checkTime();
    }

    public Interval(Year start, Year end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(true);
        this.granularity = Granularity.YEAR;
        this.checkTime();
    }

    public Interval(final Long end, Granularity granularity) {
        this.start = Utils.randomLong(0L, end);
        this.granularity = granularity;
        this.isNow = end >= Interval.MAX_SECONDS_FROM_EPOCH;
        this.end = end;
    }

    public void checkTime() {
        if (this.end < this.start) {
            throw new IllegalStateException(
                    "The finish time of the interval " + this.toString() + " is earlier than the starting time");
        }
    }

    public Optional<Interval> intersection(Interval other) {
        if (other == null) { return Optional.of(this); }
        Long start = this.start.compareTo(other.start) >= 0 ? this.start : other.start;
        Long end = this.end.compareTo(other.end) <= 0 ? this.end : other.end;
        if (start.compareTo(end) <= 0) {
            return Optional.of(
                    new Interval(start, end, this.granularity.getSmallerGranularity(other.granularity), this.isNow() && other.isNow()));
        } else {
            return Optional.empty();
        }
    }

    public int compareTinier(Interval other) {
        Long thisLength = this.end - this.start;
        Long otherLength = other.end - other.start;
        return thisLength.compareTo(otherLength);
    }

    public int compareEarlier(Interval other) {
        return this.end.compareTo(other.end);
    }

    public int compareLatestArrival(Interval other) {
        return other.end.compareTo(this.end);
    }

    public int compareLatestDeparture(Interval other) {
        return other.start.compareTo(this.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.start, this.end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Interval interval = (Interval) obj;
        return this.end.equals(interval.getEnd()) &&
                this.start.equals(interval.getStart());
    }

    @Override
    public String toString() {
        return IntervalStringifier.intervalToString(this);
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public boolean isNow () {
        return isNow;
    }

    public Comparable getStartTimeParsed() {
        return IntervalReverseParser.toTimeClass(start, granularity);
    }

    public Comparable getEndTimeParsed() {
        return IntervalReverseParser.toTimeClass(end, granularity);
    }

    public boolean contains(Long instant) {
        return instant.compareTo(start) >= 0 && instant.compareTo(end) <= 0;
    }

    public boolean contains(TimeClass timeClass, boolean intervalEnd) {
        return this.contains(timeClass.toEpochSecond(intervalEnd));
    }

    public Granularity getGranularity() {
        return this.granularity;
    }

    public boolean isBetween(Interval other) {
        return this.start.compareTo(other.start) > 0
                && this.end.compareTo(other.end) < 0;
    }

    public boolean isIntersecting(Interval other) {
        return this.intersection(other).isPresent();
    }

    public List<Interval> subtract(Interval other) {
        List<Interval> result = new ArrayList<>();
        System.out.println(this.start +", "+this.end+", "+ other.start + ", " + other.end);
        Granularity finalGranularity = granularity.getSmallerGranularity(other.granularity);
        // (keeping track of cases)
        // Cases 1 and 2: Other interval does not intersect with this one, no subtraction
        if (this.end <= other.start || this.start >= other.end) {
            return Collections.singletonList(this);
        }
        // Case 3: Other interval includes this entire interval, complete subtraction
        else if (other.start <= this.start && other.end >= this.end) {
            return Collections.emptyList();
        }
        // Case 4: Other interval starts during this one
        if (other.start <= this.end && other.start >= this.start) {
            // TODO CHECK this needs better testing and clear assumptions
            // System.out.println("Case 4");
            Interval newInterval = IntervalParser.fromString(new Interval(this.start, other.start, finalGranularity).toString());
            // System.out.println(newInterval.toString());
            result.add(newInterval);
        }
        // Case 5: Other interval ends in this one
        if (other.end >= this.start && other.end <= this.end) {
            // TODO CHECK this needs better testing and clear assumptions
            // System.out.println("Case 5");
            // System.out.println(this.toString() + " " + other.toString());
            Interval newInterval = IntervalParser.fromString(new Interval(other.end + 60, (this.end <= other.end + 60) ? this.end + 60 : this.end, finalGranularity).toString());
            // System.out.println(newInterval.toString());
            result.add(newInterval);
        }
        // Case 6: Satisfies 4 and 5
        return result;
    }

    public boolean containsInterval(Interval newInterval) {
        return this.start <= newInterval.start && this.end >= newInterval.end;
    }

    /**
     * Returns a random interval between the specified origin and the specified bound.
     *
     * @param origin lower limit of interval
     * @param bound  upper bound of interval
     * @return a random interval between the origin (inclusive) and bound (exclusive)
     */
    public static Interval randomInterval(final Long origin, Long bound) {

        if (origin >= bound) {
            throw new IllegalArgumentException("bound must be greater than origin");
        }

        Long start = Utils.randomLong(origin, bound);
        Long end = Utils.randomLong(start + 1, bound + 1);
        return new Interval(start, end, Granularity.DATE);
    }

    /**
     * Generates a list of random intervals
     * @param amount number of intervals
     * @return time interval list with stats
     * @see IntervalListWithStats
     */
    public static IntervalListWithStats list(int amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("number of intervals must be positive");
        }

        Long maxStart = 0L;
        Long minEnd = Long.MAX_VALUE;
        List<Interval> intervals = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            final int MIN_AGE = 20;
            final Long MIN_AGE_SECONDS = LocalDateTime.now().until(LocalDateTime.now().plus(Period.ofYears(20)), ChronoUnit.SECONDS);
            Long start = Utils.randomLong(0L, MAX_SECONDS_FROM_EPOCH - MIN_AGE_SECONDS);
            Interval interval = new Interval(start, Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE);
            Long end = interval.getEnd();

            if (start > maxStart) {
                maxStart = start;
            }

            if (end < minEnd) {
                minEnd = end;
            }

            intervals.add(interval);
        }

        return new IntervalListWithStats(intervals, maxStart, minEnd);
    }

    public static void main(String[] args) {
        for (int i = 0; i< 1000000; i++) {
            List<Interval> intervals = createRandomConsecutiveIntervals(IntervalParser.fromStringLimits("2000", "2022"), 10);
            IntervalSet test = new IntervalSet(intervals.get(0));
            for (int j = 0; j < intervals.size(); j++) {
                test = test.intersection(intervals.get(j));
            }
            if (!test.isEmpty())
            System.out.println("Error");
        } 
    }

    /**
     * Creates a number of disjoint random intervals within another interval.
     *
     * @param timeInterval      main interval
     * @param maxNumberOfIntervals amount of intervals
     * @return list of intervals
     */
    public static List<Interval> createRandomDisjointIntervals(Interval timeInterval, int maxNumberOfIntervals) {

        if (intervalIsSmallerThanNumberOfIntervals(timeInterval, maxNumberOfIntervals)) {
            return Collections.singletonList(new Interval(timeInterval.getStart(), timeInterval.getEnd(), Granularity.DATE));
        }

        // System.out.println("Creating random disjoint intervals for interval: " + timeInterval.toString());

        List<Interval> intervals = new ArrayList<>();

        Long start = timeInterval.getStart();
        Long end = timeInterval.getEnd();

        for (int i = 0; i < maxNumberOfIntervals; i++) {
            Interval interval = Interval.randomInterval(start, end);
            intervals.add(interval);
            start = interval.getEnd() + 60 * 60 * 24 + 1;
            if (interval.getEnd() >= timeInterval.getEnd() || start >= end) {
                break;
            }
        }

        return intervals;
    }

    /**
     * Creates random consecutive intervals within another interval
     * @param timeInterval base interval
     * @param numberOfIntervals how many intervals
     * @return a list of consecutive random intervals within another one
     */
    public static List<Interval> createRandomConsecutiveIntervals(Interval timeInterval, int numberOfIntervals) {

        List<Interval> intervals = new ArrayList<>();

        Long start = timeInterval.getStart();

        if (intervalIsSmallerThanNumberOfIntervals(timeInterval, numberOfIntervals)) {
            return Collections.singletonList(new Interval(timeInterval.getStart(), timeInterval.getEnd(), Granularity.DATE));
        }

        Long lowerBound = start + 60 * 60 * 24 + 1;
        Long upperBound = timeInterval.getEnd() - 1 <= lowerBound ? lowerBound + 61 : timeInterval.getEnd() - 1;

        List<Long> breakPoints = Utils.randomConsecutiveLong(lowerBound, upperBound, numberOfIntervals - 1);

        for (Long end : breakPoints) {
            intervals.add(new Interval(start, end, Granularity.DATE));
            start = end + 60 * 60 * 24 + 61 + 1;
        }

        //Last interval
        intervals.add(new Interval(start, timeInterval.getEnd() < start? start + 60*60*24+61 : timeInterval.getEnd(), Granularity.DATE));

        return intervals;
    }

    public static boolean intervalIsSmallerThanNumberOfIntervals(Interval interval, int numberOfIntervals) {
        return DateTime.fromEpochSecond(interval.getEnd()).asDateTime().getYear() - DateTime.fromEpochSecond(interval.getStart()).asDateTime().getYear() <= numberOfIntervals;
    }

    public Interval getRandomSubInterval() {
        Long subIntervalStart = Utils.randomLong(start, end);
        Long subIntervalEnd = Utils.randomLong(subIntervalStart, end);
        return new Interval(subIntervalStart, subIntervalEnd, Granularity.DATE);
    }
}
