package ar.edu.itba.util.interval;

import ar.edu.itba.util.time.DateTime;

public class IntervalReverseParser {

    static Comparable toTimeClass(Long timestamp, Granularity granularity) {
        DateTime dateTime = DateTime.fromEpochSecond(timestamp);
        switch (granularity) {
            case DATETIME:
                return dateTime;
            case DATE:
                return dateTime.getDate();
            case YEAR_MONTH:
                return dateTime.getYearMonth();
            case YEAR:
                return dateTime.getYear();
        }
        throw new IllegalArgumentException("Could not parse granularity.");
    }

}
