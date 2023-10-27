package ar.edu.itba.util;

import ar.edu.itba.grammar.struct.Granularity;
import ar.edu.itba.util.interval.Interval;
import ar.edu.itba.util.interval.IntervalParser;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

public class Time {

    private static final String YEAR_FORMAT = "uuuu[-MM]";
    private static final String DATE_FORMAT = "uuuu-MM-dd[ HH:mm]";

    private static final DateTimeFormatter YEAR_FORMATTER  = yearFormatter();
    private static final DateTimeFormatter DATE_FORMATTER  = dateFormatter();

    /**
     * Validates the date against a format
     * @param time time to be validated
     * @return whether this string is valid or not
     */
    public static boolean validateTime(final String time) {
        try {
            LocalDateTime.parse(time, getFormatter(time));
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Validates a date interval
     * @param from date from of interval
     * @param to date to of interval
     * @return whether this date interval is valid or not
     */
    public static boolean validateTimeInterval(final String from, final String to) {
        IntervalParser.fromStringLimits(from, to);
        return true;
    }

    /**
     * @param time date in string format
     * @return java util date in one of the two formats supported by TempoGraph
     */
    private static LocalDateTime getTime(final String time) {
        if (time.toUpperCase().equals(Granularity.NOW)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(time, getFormatter(time));
        } catch (DateTimeParseException e) {
            throw new ParseCancellationException("Invalid date format");
        }
    }

    private static DateTimeFormatter yearFormatter() {
        return new DateTimeFormatterBuilder()
                .appendPattern(YEAR_FORMAT)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
    }

    private static DateTimeFormatter dateFormatter() {
        return new DateTimeFormatterBuilder()
                .appendPattern(DATE_FORMAT)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
    }

    private static DateTimeFormatter getFormatter(String time) {
        if (time.length() <= YEAR_FORMAT.length())
            return YEAR_FORMATTER;
        else
            return DATE_FORMATTER;
    }

}
