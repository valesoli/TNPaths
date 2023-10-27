package ar.edu.itba.procedures.cotemporal;

import ar.edu.itba.algorithms.GraphAlgorithm;
import ar.edu.itba.algorithms.strategies.graph.BFSIterationExpansionStrategy;
import ar.edu.itba.algorithms.utils.GraphSaver;
import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.GraphBuilder;
import ar.edu.itba.records.GraphRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class GraphProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="coexisting.graphs")
    @Description("Get a graph that contains all the coexisting paths of length n or less.")
    public Stream<GraphRecord> graph(
            @Name("node query") Node node,
            @Name("length") Long length,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        log.info("Initializing coexisting graph algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).build(new ProcedureConfiguration(configuration), false);
        GraphAlgorithm algorithm = new GraphAlgorithm(graph)
                .setStrategy(new BFSIterationExpansionStrategy())
                .setLog(log)
                .setInitialNode(node)
                .setFinishingCondition(iteration -> iteration >= length);
        GraphSaver result = algorithm.run();

        timer.stop();
        log.info(String.format("Algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        Stream<GraphRecord> edges =
                result.getEdges().stream().map(id -> new GraphRecord(db.getRelationshipById(id)));
        Stream<GraphRecord> nodes =
                result.getNodes().stream().map(id -> new GraphRecord(db.getNodeById(id)));
        return Stream.concat(nodes, edges);
    }

}
