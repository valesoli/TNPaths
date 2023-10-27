package ar.edu.itba.population;

import ar.edu.itba.util.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class DateTimeInterval {

    private LocalDateTime start;
    private LocalDateTime end;

    private static final String SEPARATOR = "—";

    public DateTimeInterval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Creates consecutive random intervals
     * @param intervalAmount number of intervals to be created
     * @param start time of first flight
     * @param minutesBetweenIntervals number of minutes between each interval
     * @return a list of consecutive intervals of equal duration
     */
    public static List<DateTimeInterval> randomDateTimeIntervals(final int intervalAmount, final LocalDateTime start, int minutesBetweenIntervals) {
        List<DateTimeInterval> list = new ArrayList<>(intervalAmount);

        if (intervalAmount <= 0) {
            return list;
        }

        int minDuration = 2;
        int maxDuration = 10;
        int durationInHours = Utils.randomInteger(minDuration, maxDuration);

        LocalDateTime starTime = start;
        LocalDateTime endTime = start.plusHours(durationInHours);
        DateTimeInterval interval = new DateTimeInterval(starTime, endTime);
        list.add(interval);

        for (int i = 0; i < intervalAmount - 1; i++) {
            starTime = endTime.plusHours(minutesBetweenIntervals);
            endTime = starTime.plusHours(durationInHours);
            interval = new DateTimeInterval(starTime, endTime);
            list.add(interval);
        }

        return list;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                        .append(formatter().format(start))
                        .append(SEPARATOR)
                        .append(formatter().format(end))
                        .toString();
    }

    private static DateTimeFormatter formatter() {
        return new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart()
                .appendPattern(" HH:mm")
                .optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .toFormatter();
    }
}
