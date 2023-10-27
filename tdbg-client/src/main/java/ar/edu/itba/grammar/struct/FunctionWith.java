package ar.edu.itba.grammar.struct;

public class FunctionWith {
    protected String variable;

    public FunctionWith(String variable) {
        this.variable = variable;
    }

    public String toString() {
        return variable;
    }

    public boolean unwindable() {return false;}
}
