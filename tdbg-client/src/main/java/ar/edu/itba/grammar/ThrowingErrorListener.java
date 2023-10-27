package ar.edu.itba.grammar;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class ThrowingErrorListener extends BaseErrorListener {

    private static final ThrowingErrorListener instance = new ThrowingErrorListener();

    private ThrowingErrorListener() {

    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        final StringBuilder s = new StringBuilder();
        s
            .append("Error at line ")
            .append(line)
            .append(" near position ")
            .append(charPositionInLine)
            .append(", ")
            .append(msg);
        throw new ParseCancellationException(s.toString());
    }

    public static ThrowingErrorListener getInstance() {
        return instance;
    }
}
