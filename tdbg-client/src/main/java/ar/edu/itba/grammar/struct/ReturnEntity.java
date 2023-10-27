package ar.edu.itba.grammar.struct;

/**
 * We use RETURN * to get the result from the non temporal query
 * We filter the previous result using temporal conditions
 * This class is used to return the objects indicated by the user in SELECT
 */

public class ReturnEntity {
    private final String name;
    private final String alias;

    public ReturnEntity(final String name, final String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getId() {
        return alias.isEmpty()? name : alias;
    }
}
