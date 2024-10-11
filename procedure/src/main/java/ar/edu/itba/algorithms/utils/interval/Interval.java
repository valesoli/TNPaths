package ar.edu.itba.algorithms.utils.interval;

import ar.edu.itba.algorithms.utils.time.*;

import java.time.Duration;
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

    public Interval(Long start, Long end, Granularity granularity) {
        this.start = start;
        this.end = end;
        this.granularity = granularity;
        this.checkTime();
    }

    public Interval(Long start, Long end, Granularity granularity, boolean isNow) {
        this(start, end, granularity);
        this.isNow = isNow;
    }

    public Interval(DateTime start, DateTime end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(false);
        this.granularity = Granularity.DATETIME;
        this.checkTime();
    }

    public Interval(Date start, Date end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(false);
        this.granularity = Granularity.DATE;
        this.checkTime();
    }

    public Interval(YearMonth start, YearMonth end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(false);
        this.granularity = Granularity.YEAR_MONTH;
        this.checkTime();
    }

    public Interval(Year start, Year end) {
        this.start = start.toEpochSecond(false);
        this.end = end.toEpochSecond(false);
        this.granularity = Granularity.YEAR;
        this.checkTime();
    }

    public void checkTime() {
        if (this.end <= this.start) {
            throw new IllegalStateException(
                    "The finish time of the interval " + this.toString() + " is earlier than the starting time");
        }
    }

    public Optional<Interval> intersection(Interval other) {
        if (other == null) { return Optional.of(this); }
        Long start = this.start.compareTo(other.start) >= 0 ? this.start : other.start;
        Long end = this.end.compareTo(other.end) <= 0 ? this.end : other.end;
        
        if (start.compareTo(end) < 0) {
        	return Optional.of(
                    new Interval(start, end, this.granularity.getSmallerGranularity(other.granularity), 
                    		this.isNow() && other.isNow()));
        } else {
            return Optional.empty();
        }
    }
//Vale
    public Optional<Interval> consecutive(Interval other) {
        if (other == null) { return Optional.of(this); }
        return Optional.of(other);
        /*if (this.start.compareTo(other.start) > 0) {
            return Optional.of(other);
        } else {
            return Optional.empty();
        }*/
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
    
    public boolean compareNextDelta(Interval other, Duration delta) {
    	//return (other.start > this.end && other.start < this.end + delta.getSeconds() );
    	return (this.start < other.start && other.start < this.start + delta.getSeconds());
       
    }
    
    public boolean compareNextDeltaBack(Interval other, Duration delta) {
    	//return (other.start > this.end && other.start < this.end + delta.getSeconds() );
    	return (this.start > other.start && this.start - other.start < delta.getSeconds()  );
       
    }
    
    public boolean compareDelta(Interval other, Duration delta) {
    	//return (other.start > this.end && other.start < this.end + delta.getSeconds() );
    	return ((this.start <= other.start && 
    			other.start < this.start + delta.getSeconds())||
    			(this.start >= other.start && 
    			this.start - other.start < delta.getSeconds() )
    			);
       
    }
    
    public boolean startsAfter(Interval other) {
    	return (other.start < this.start);
       
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
        // System.out.println(this.toString() + " contains " + newInterval.toString() + "? " + (this.start <= newInterval.start && this.end >= newInterval.end));
        return this.start <= newInterval.start && this.end >= newInterval.end;
    }
    
   public static void main(String[] args) {
	   Interval interval = new Interval(new Year(2020), new Year(2022));
	   Interval interval2 = new Interval(new Year(2022), new Year(2024));
	   System.out.println(interval);
	   System.out.println(interval.intersection(interval2));
   }
}
