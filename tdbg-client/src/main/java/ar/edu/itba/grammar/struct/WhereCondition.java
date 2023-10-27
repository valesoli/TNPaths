package ar.edu.itba.grammar.struct;

import java.util.Set;

public class WhereCondition extends BaseWhereCondition {

    /**
     * @param variables
     * @return if all the variables in this condition are contained in input
     */
    public boolean containedIn(final Set<String> variables) {
        for (String v : conditionVariables) {
            if (!variables.contains(v)) {
                return false;
            }
        }
        return true;
    }
}
