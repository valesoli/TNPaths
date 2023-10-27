package ar.edu.itba.config.constants;

/**
 * Represents the type of projection to apply to the graph in order to execute the algorithm over all the graph, some
 * edges and nodes with a specific label, or a subgraph. This values are received in the configuration of the procedure
 * and the string that contains them is the value of the string received.
 */
public enum ProjectionType implements TextSearchType {

    /**
     * FULL means that the projection is done over all the graph.
     */
    FULL("full"),
    /**
     * QUERY means that the projection is done over a query that represents a subgraph of that graph.
     */
    QUERY("query"),
    /**
     * LABEL means that the projection is done over some nodes and edges with a specific label in the graph.
     */
    LABEL("label");

    private final String text;

    ProjectionType(String text) {
        this.text = text;
    }

    /**
     * Returns the text contained in the ProjectionType. This texts should coincide with the one received in the
     * configuration.
     *
     * @return the text contained in the instance.
     */
    @Override
    public String getText() {
        return this.text;
    }

    /**
     * Returns the ProjectionType from a string that represents its type. If the string does not coincide with any type
     * it returns null.
     *
     * @param type an String that should coincide with a ProjectionType.
     * @return the ProjectionType that coincides with the type String.
     */
    public static ProjectionType fromString(String type) {
        return (ProjectionType) TextSearchEnumHelper.fromString(ProjectionType.values(), type);
    }
}
