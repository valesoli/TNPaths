package ar.edu.itba.grammar.struct;

import java.util.HashSet;
import java.util.Set;

public class WhenWhereCondition extends BaseWhereCondition {

    /**
     * @param variables
     * @return variables that are not included in input
     */

    public Set<String> getMissingVariables(final Set<String> variables) {
        final Set<String> missingDefinitions = new HashSet<>();
        for (String variable : conditionVariables) {
            if (!variables.contains(variable))
                missingDefinitions.add(variable);
        }
        return missingDefinitions;
    }
}
