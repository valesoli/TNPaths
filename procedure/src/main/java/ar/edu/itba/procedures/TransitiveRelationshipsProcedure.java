package ar.edu.itba.procedures;

import ar.edu.itba.algorithms.GraphAlgorithm;
import ar.edu.itba.algorithms.strategies.graph.BFSWithIntervalPruneStrategy;
import ar.edu.itba.algorithms.utils.GraphSaver;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.GraphBuilder;
import ar.edu.itba.records.GraphRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class TransitiveRelationshipsProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="coexisting.transitiveRelationships")
    public Stream<GraphRecord> transitiveRelationship(
            @Name("initial node") Node node,
            @Name("length") Long length,
            @Name("interval") String interval,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        log.info("Initializing transitive relationships procedure.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).build(new ProcedureConfiguration(configuration), false);
        GraphAlgorithm algorithm = new GraphAlgorithm(graph)
                .setStrategy(new BFSWithIntervalPruneStrategy(IntervalParser.fromString(interval)))
                .setLog(log)
                .setInitialNode(node)
                .setFinishingCondition(iteration -> length != 0 && iteration >= length);
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
