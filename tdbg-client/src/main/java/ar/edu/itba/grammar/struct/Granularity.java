package ar.edu.itba.grammar.struct;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Arrays;
import java.util.Optional;

public enum Granularity {
    YEAR("uuuu", "0001"),
    YEAR_MONTH("uuuu-MM", "0001-01"),
    DATE("uuuu-MM-dd", "0001-01-01"),
    DATETIME("uuuu-MM-dd HH:mm", "0001-01-01 00:00");

    private final String format;
    private final String minValue;
    public static final String NOW = "NOW";

    Granularity(final String format, final String minValue) {
        this.format = format;
        this.minValue = minValue;
    }

    private static Granularity getGranularity(String time) {
        final int length = time.length();
        Optional<Granularity> granularity = Arrays.stream(values()).filter(g -> g.format.length() == length).findFirst();
        if (!granularity.isPresent())
            throw new ParseCancellationException("Invalid granularity");
        return granularity.get();
    }

    public static boolean validateTimeInterval(String from, String to) {
        if (to.toUpperCase().equals(NOW)) {
            return true;
        }
        return from.length() == to.length();
    }

    public static String getMinValue(String value) {
        return getGranularity(value).minValue;
    }
}
