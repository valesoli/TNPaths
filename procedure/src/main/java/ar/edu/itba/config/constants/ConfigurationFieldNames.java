package ar.edu.itba.config.constants;

/**
 * This class holds all the constants that are names of configuration fields, that means, in the map that holds the
 * configuration, you can access one of this values by using one of this names. This class is not instantiable.
 */
public class ConfigurationFieldNames {

    public static final String DIRECTION = "direction";
    public static final String GRAPH = "graph";
    public static final String NODE_LABEL = "nodesLabel";
    public static final String EDGE_LABEL = "edgesLabel";
    public static final String NODE_WEIGHT = "nodeIntervals";
    public static final String EDGE_WEIGHT = "edgeIntervals";
    public static final String QUERY = "query";
    public static final String BETWEEN = "between";
    public static final String ATTRIBUTE = "attribute";
    public static final String OPERATOR = "operator";
    public static final String VALUE = "value";
    public static final String PERIOD = "period";
    public static final String ALPHAS = "alphas";
    

    private ConfigurationFieldNames() {
        throw new UnsupportedOperationException("This class should not be instantiated!");
    }
}
