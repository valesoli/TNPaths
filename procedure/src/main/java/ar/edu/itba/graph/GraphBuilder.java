package ar.edu.itba.graph;

import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.graph.impl.*;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * This class is an auxiliary class to create a Graph object. Graphs have many options as to manipulate and store the
 * data that receives from the configuration.
 */
public class GraphBuilder {

    private final GraphDatabaseService db;
    private static final String DEFAULT_WEIGHT_ATTR= "interval";

    /**
     * Creates a GraphBuilder.
     *
     * @param db a GraphDatabaseService to make requests.
     */
    public GraphBuilder(
            GraphDatabaseService db
    ) {
        this.db = db;
    }

    /**
     * Sets the configuration received by the procedure.
     *
     * @param configuration the configuration received.
     * @return this factory.
     */
    public Graph build(ProcedureConfiguration configuration, boolean pruneBetween) {
        Graph graph;
        switch (configuration.getProjection()) {
            case LABEL:
                graph = new PartialGraph(
                        this.db,
                        configuration.getStringFromKey(ConfigurationFieldNames.NODE_LABEL),
                        configuration.getStringFromKey(ConfigurationFieldNames.EDGE_LABEL),
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getDirection(),
                        configuration.getIntervalLimit(),
                        pruneBetween
                );
                break;
            case FULL:
                graph = new FullGraph(
                        this.db,
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getDirection(),
                        configuration.getIntervalLimit(),
                        pruneBetween
                );
                break;
            case QUERY:
                graph = new LightGraph(
                        this.db,
                        configuration.getStringFromKey(ConfigurationFieldNames.QUERY),
                        configuration.getDirection(),
                        configuration.getIntervalLimit(),
                        pruneBetween
                );
                break;
            default:
                throw new IllegalStateException("Not valid projection type");
        }
        return graph;
    }

    /**
     * Sets the configuration received by the procedure.
     *
     * @param configuration the configuration received.
     * @return this factory.
     */
    public Graph buildStored(ProcedureConfiguration configuration, boolean pruneBetween) {
        Graph graph;
        switch (configuration.getProjection()) {
            case LABEL:
                graph = new PartialStoredGraph(
                        this.db,
                        configuration.getStringFromKey(ConfigurationFieldNames.NODE_LABEL),
                        configuration.getStringFromKey(ConfigurationFieldNames.EDGE_LABEL),
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getDirection(),
                        configuration.getIntervalLimit(),
                        pruneBetween
                );
                break;
            case FULL:
                graph = new FullStoredGraph(
                        this.db,
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getOrElse(ConfigurationFieldNames.NODE_WEIGHT, DEFAULT_WEIGHT_ATTR),
                        configuration.getDirection(),
                        configuration.getIntervalLimit(),
                        pruneBetween
                );
                break;
            case QUERY:
                graph = new LightGraph(
                        this.db,
                        configuration.getStringFromKey(ConfigurationFieldNames.QUERY),
                        configuration.getDirection(),
                        configuration.getIntervalLimit(),
                        pruneBetween
                );
                break;
            default:
                throw new IllegalStateException("Not valid projection type");
        }
        return graph;
    }

}
