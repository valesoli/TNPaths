package ar.edu.itba.grammar.struct;

public enum CharConstants {
    DASH('-'),
    UNDERSCORE('_'),
    LONG_DASH('—'),
    OPENING_PARENTHESIS('('),
    CLOSING_PARENTHESIS(')'),
    OPENING_BRACKET('{'),
    CLOSING_BRACKET('}'),
    QUOTE('\''),
    DOUBLE_QUOTE('\"'),
    ASTERISK('*'),
    POINT('.'),
    COLON(':'),
    SEMICOLON(';'),
    LINE_SEPARATOR('\n'),
    COMMA(','),
    SPACE(' '),
    BACKSLASH('\\'),
    GRAVE_ACCENT('`');

    private final char value;

    CharConstants(final char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }
}
