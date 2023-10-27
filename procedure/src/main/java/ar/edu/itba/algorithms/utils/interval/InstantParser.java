package ar.edu.itba.algorithms.utils.interval;

import ar.edu.itba.algorithms.utils.time.Date;
import ar.edu.itba.algorithms.utils.time.DateTime;
import ar.edu.itba.algorithms.utils.time.Year;
import ar.edu.itba.algorithms.utils.time.YearMonth;
import org.apache.commons.lang3.tuple.Pair;

import java.time.format.DateTimeParseException;

public class InstantParser {

    public static Pair<Long, Granularity> parse(String value) {
        return parse(value, false);
    }

    public static Pair<Long, Granularity> parse(String value, boolean intervalEnd) {
        try {
            DateTime datetime = DateTime.parse(value);
            return Pair.of(datetime.toEpochSecond(intervalEnd), Granularity.DATETIME);
        } catch (DateTimeParseException e){
            return parseYearMonth(value, intervalEnd);
        }
    }

    private static Pair<Long, Granularity> parseYearMonth(String value, boolean intervalEnd) {
        try {
            YearMonth yearMonth = YearMonth.parse(value);
            return Pair.of(yearMonth.toEpochSecond(intervalEnd), Granularity.YEAR_MONTH);
        } catch (DateTimeParseException e1) {
            return parseDate(value, intervalEnd);
        }
    }

    private static Pair<Long, Granularity> parseDate(String value, boolean intervalEnd) {
        try {
            Date date = Date.parse(value);
            return Pair.of(date.toEpochSecond(intervalEnd), Granularity.DATE);
        } catch (DateTimeParseException e2) {
            return parseYear(value, intervalEnd);
        }
    }

    private static Pair<Long, Granularity> parseYear(String value, boolean intervalEnd) {
        Year year = Year.parse(value);
        return Pair.of(year.toEpochSecond(intervalEnd), Granularity.YEAR);
    }

    public static Pair<Long, Granularity> nowValue(Granularity granularity) {
        Long timeStamp;
        switch (granularity) {
            case DATETIME:
                timeStamp = DateTime.now().toEpochSecond(true);
                break;
            case DATE:
                timeStamp = Date.now().toEpochSecond(true);
                break;
            case YEAR_MONTH:
                timeStamp = YearMonth.now().toEpochSecond(true);
                break;
            case YEAR:
                timeStamp = Year.now().toEpochSecond(true);
                break;
            default:
                throw new IllegalArgumentException("Could not parse granularity.");
        }
        return Pair.of(timeStamp, granularity);
    }

    public static Granularity getGranularity(String value) {
        try {
            DateTime.parse(value);
            return Granularity.DATETIME;
        } catch (DateTimeParseException e){
            return Granularity.YEAR;
        }
    }
}
