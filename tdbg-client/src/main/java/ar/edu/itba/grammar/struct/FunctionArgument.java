package ar.edu.itba.grammar.struct;

public class FunctionArgument {

    private String value;
    private boolean string;

    public FunctionArgument(String value) {
        this.value = value;
        this.string = true;
    }

    public FunctionArgument(String value, boolean string) {
        this.value = value;
        this.string = string;
    }

    public String getValue() {
        return value;
    }

    public boolean isString() {
        return string;
    }
}
