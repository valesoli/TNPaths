package ar.edu.itba.grammar.struct;

import ar.edu.itba.grammar.CypherOutputGenerator;
import ar.edu.itba.grammar.struct.params.FunctionParams;

import java.util.Map;
import java.util.Set;

public class Value {

    private final Attribute attribute;

    private final String name;

    private String alias;

    /**
     * Definition of the path to this value node
     * Example: p1 --> (n:Attribute {title: 'Name'} --> (v:Value)
     */
    private final String definition;

    /**
     * If the attribute was used in select and the index of selection
     */
    private Integer selectIndex;

    private boolean foundInSelect;

    public Value(final Attribute attribute, final String name, final String definition, boolean foundInSelect) {
        this.attribute = attribute;
        this.name = name;
        this.definition = definition;
        this.foundInSelect = foundInSelect;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isSelected() {
        return selectIndex != null;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setSelectIndex(Integer index) {
        this.selectIndex = index;
    }

    private String getAttrSelectWithAlias(final String variable) {
        return variable + CharConstants.POINT + attribute.getName() + (alias != null ? CypherOutputGenerator.getAliasString(alias) : "");
    }

    private String getUserAttrSelect(final String variable) {
        return variable + CharConstants.POINT + attribute.getName();
    }

    public Integer getSelectIndex() {
        return selectIndex;
    }

    public void appendToReturn(final Set<String> functionVariables, final Set<String> edgesVariable,
                               final Map<Integer, ReturnEntity> returnEntities, final Map<Integer, String> returnExpr) {
        if (!isSelected())
            return;

        final String variable = attribute.getVariable().getName();
        if (functionVariables.contains(variable) || edgesVariable.contains(variable)) {
            returnExpr.put(selectIndex, getAttrSelectWithAlias(variable));
            returnEntities.put(selectIndex, new ReturnEntity(variable + CharConstants.POINT + attribute.getName(), alias != null? alias : ""));
        }
        else {
            if (!functionVariables.isEmpty())
                FunctionParams.addSelectedVariable(name);

            if (alias == null) {
                final String alias = getUserAttrSelect(variable);
                returnExpr.put(selectIndex, name + CypherOutputGenerator.getAliasString(alias));
                returnEntities.put(selectIndex, new ReturnEntity(name, alias));
            }
            else {
                returnExpr.put(selectIndex, name);
                returnEntities.put(selectIndex, new ReturnEntity(name, ""));
            }
        }
    }

    public boolean foundInSelect() {
        return foundInSelect;
    }

    public void setFoundInSelect(boolean foundInSelect) {
        this.foundInSelect = foundInSelect;
    }
}
