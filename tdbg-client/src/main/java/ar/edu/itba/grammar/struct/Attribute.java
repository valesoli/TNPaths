package ar.edu.itba.grammar.struct;

public class Attribute {

    private final String name;

    private final Variable variable;

    private final Value value;

    public Attribute(final Variable variable, final String name, final String valueName, final String definition,
                     final boolean foundInSelect) {
        this.name = name;
        this.variable = variable;
        this.value = new Value(this, valueName, definition, foundInSelect);
    }

    public String getName() {
        return name;
    }

    public Variable getVariable() {
        return variable;
    }

    public Value getValue() {
        return value;
    }
}
