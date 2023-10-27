package ar.edu.itba.grammar.struct;

import ar.edu.itba.grammar.CypherOutputGenerator;

import java.util.*;

public class ParserVariables {

    /**
     * Auto incremental map
     * Key: first character of an input received in {@link #createVariable(String)}
     * Value: auto incremental
     */
    private final Map<Character, Long> variableCount = new HashMap<>();

    private static final String INTERNAL = "internal";

    /**
     * @param s string from which the variable is generated
     * Example: Name -> n1
     * @return new variable string
     */
    public String createVariable(final String s) {
        final char firstLetter = Character.toLowerCase(s.charAt(0));

        if (variableCount.containsKey(firstLetter))
            variableCount.put(firstLetter, variableCount.get(firstLetter) + 1);
        else
            variableCount.put(firstLetter, 0L);
        final long count = variableCount.get(firstLetter);
        return INTERNAL + CharConstants.UNDERSCORE + firstLetter + count;
    }

    public String createVariable() {
        return createVariable(CypherOutputGenerator.INTERNAL);
    }
}
