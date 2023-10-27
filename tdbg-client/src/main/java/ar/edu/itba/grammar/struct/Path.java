package ar.edu.itba.grammar.struct;

import java.util.Set;

public class Path {
    private final Set<String> objectVariables;
    private final String definition;
    private boolean addedToWhen;

    public Path(final Set<String> objectVariables, final String definition) {
        this.objectVariables = objectVariables;
        this.definition = definition;
        addedToWhen = false;
    }

    public Set<String> getObjectVariables() {
        return objectVariables;
    }

    public String getDefinition() {
        return definition;
    }

    public boolean isAddedToWhen() {
        return addedToWhen;
    }

    public void setAddedToWhen(final boolean bool) {
        this.addedToWhen = bool;
    }
}
