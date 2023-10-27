package ar.edu.itba.util;

import ar.edu.itba.grammar.struct.CharConstants;
import org.antlr.v4.runtime.Token;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Utils {

    /**
     * Generates an error message that is thrown in an parser exception
     * @see ParseException
     * @param message error message
     * @param token token used to get line number and position in line of the token that generated the error
     * @return string error message
     */
    public static String generateErrorMessage(final String message, final Token token) {
        return new StringBuilder()
                .append("Error at line ")
                .append(token.getLine())
                .append(" near position ")
                .append(token.getCharPositionInLine())
                .append(", ")
                .append(message)
                .toString();
    }

    public static String getQuotedString(final String s) {
        if (!isQuoted(s))
            throw new IllegalArgumentException("Not a quoted string");
        return s.substring(1, s.length() - 1);
    }

    private static boolean isQuoted(final String s) {
        return s.charAt(0) == CharConstants.QUOTE.getValue() && s.charAt(s.length() - 1) == CharConstants.QUOTE.getValue()
                && s.chars().filter(ch -> ch == CharConstants.QUOTE.getValue()).count() == 2;
    }

    /**
     * Returns an integer between a min (inclusive) and a max value (exclusive).
     * @param min lower bound
     * @param max upper bound
     * @return a random integer between [min, max)
     */
    public static int randomInteger(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static Long randomLong(Long min, Long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * Returns ordered unique random numbers.
     * @param start the lower bound (inclusive)
     * @param end the upper bound (exclusive)
     * @param numbers how many numbers
     * @return a list of ordered unique random integers.
     */
    public static List<Integer> randomConsecutive(int start, int end, int numbers) {

        if (start > end) {
            throw new IllegalArgumentException(String.format("Start (%d) must be smaller than end (%d)", start, end));
        }

        if (end - start < numbers) {
            throw new IllegalArgumentException();
        }

        List<Integer> is = IntStream.range(start, end).boxed().collect(Collectors.toList());
        Collections.shuffle(is);
        List<Integer> randomIndexes = is.subList(0, numbers);
        Collections.sort(randomIndexes);
        return randomIndexes;
    }

    public static List<Long> randomConsecutiveLong(Long start, Long end, int numbers) {

        if (start > end) {
            throw new IllegalArgumentException(String.format("Start (%d) must be smaller than end (%d)", start, end));
        }

        if (end - start < numbers) {
            throw new IllegalArgumentException();
        }

        Long maxDistance = (end - start) / numbers;
        if (maxDistance < 60 * 60 * 24) {
            throw new IllegalStateException("Cannot generate enough consecutive longs in interval");
        }
        List<Long> is = new ArrayList<>();
        Long min = start;
        for (int i = 0; i < numbers; i++) {
            Long auxMin = ThreadLocalRandom.current().nextLong(min, min + maxDistance);
            is.add(auxMin);
            min = auxMin + 60 * 60 * 24;
        }

        // LongStream.range(start, end).boxed().collect(Collectors.toList());
        // Collections.shuffle(is);
        // List<Long> randomIndexes = is.subList(0, numbers);
        
        Collections.sort(is);
        return is;
    }

}
