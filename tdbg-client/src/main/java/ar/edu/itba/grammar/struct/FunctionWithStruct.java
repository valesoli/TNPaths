package ar.edu.itba.grammar.struct;

import ar.edu.itba.grammar.CypherOutputGenerator;
import ar.edu.itba.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class FunctionWithStruct extends FunctionWith {
    private final List<Pair<String, String>> keyValue = new LinkedList<>();
    private AggregateFunction function;

    public FunctionWithStruct(final String callVariable) {
        super(callVariable);
    }

    public void addPair(final String key, final String value) {
        keyValue.add(new Pair<>(key, value));
    }

    public void setFunction(final String function) {
        this.function = AggregateFunction.getFromFunctionName(function);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (this.function != null) {
            sb.append(function);
            sb.append(CharConstants.OPENING_PARENTHESIS);
        }
        sb.append(CharConstants.OPENING_BRACKET);
        int i = 0;
        for (final Pair<String, String> p : keyValue) {
            sb.append(p.getLeft())
                    .append(CharConstants.COLON)
                    .append(CharConstants.SPACE)
                    .append(p.getRight());
            if (i != keyValue.size() - 1) {
                sb.append(CharConstants.COMMA);
                sb.append(CharConstants.SPACE);
            }
            i++;
        }

        sb.append(CharConstants.CLOSING_BRACKET);

        if (this.function != null) {
            sb.append(CharConstants.CLOSING_PARENTHESIS);
        }

        sb.append(CharConstants.SPACE)
                .append(CypherOutputGenerator.AS)
                .append(CharConstants.SPACE)
                .append(variable);

        if (this.function != null) {
            sb.append(CharConstants.UNDERSCORE);
        }

        return sb.toString();
    }

    public boolean unwindable() {
        return true;
    }

    public String unwindString() {
        if (this.function == null) {
            return "";
        }
        return new StringBuilder()
                .append(CypherOutputGenerator.UNWIND)
                .append(CharConstants.SPACE)
                .append(variable)
                .append(CharConstants.UNDERSCORE)
                .append(CharConstants.SPACE)
                .append(CypherOutputGenerator.AS)
                .append(CharConstants.SPACE)
                .append(variable)
                .append(CharConstants.LINE_SEPARATOR)
                .toString();
    }
}
