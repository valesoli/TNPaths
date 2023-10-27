package ar.edu.itba.grammar.struct;

import java.util.Set;

public abstract class BaseWhereCondition {
    protected Set<String> conditionVariables;
    protected String condition;

    public void setConditionVariables(Set<String> conditionVariables) {
        this.conditionVariables = conditionVariables;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}
