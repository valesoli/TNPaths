package ar.edu.itba.procedures.consecutive;

import ar.edu.itba.algorithms.TimeNodePathAlgorithm;
import ar.edu.itba.algorithms.strategies.path.PathStrategy;
import ar.edu.itba.algorithms.strategies.path.consecutive.ComparatorPruneConsecutiveStrategy;
import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;
import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.GraphBuilder;
import ar.edu.itba.records.TemporalPathRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class FastestPathProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="consecutive.fastest")
    @Description("Get the path with the fastest interval.")
    public Stream<TemporalPathRecord> fastest(
            @Name("initial node") Node initialNode,
            @Name("ending node") Node endingNode,
            @Name("consecutive difference") Long difference,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        log.info("Initializing coexisting graph algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        PathStrategy<Long> strategy = new ComparatorPruneConsecutiveStrategy(
                log,
                (n1, n2) -> {
                    if (n1.getPayload() == null && n2.getPayload() == null)
                        return 0;
                    if (n1.getPayload() == null)
                        return -1;
                    if (n2.getPayload() == null)
                        return 1;
                    return Comparator.comparingLong((TimeNode<Long> t) -> t.getTime() - t.getPayload()).compare(n2, n1);
                },
                (solution, other) -> (solution.getTime() - solution.getPayload()) <
                        (other.getTime() - other.getPayload()),
                difference,
                (n1, n2) -> n2.setPayload(n1.getPayload())
        );

        Graph graph = new GraphBuilder(db).build(new ProcedureConfiguration(configuration), true);
        TimeNodePathAlgorithm<Long> algorithm = new TimeNodePathAlgorithm<Long>(graph)
                .setLog(log)
                .setStrategy(strategy)
                .setInitialNode(initialNode)
                .setFinishNode(endingNode);
        List<TimeNode<Long>> results = algorithm.run();

        timer.stop();
        log.info(String.format("Algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        log.info(String.format("Nodes expanded %d.", strategy.getNodesExpanded()));
        int size = results == null ? 0 : results.size();
        log.info(String.format("%d results.", size));

        if (results == null || results.isEmpty())
            return Stream.empty();
        return TemporalPathRecord.getRecordsFromTimeNodeList(
                results, db, graph.getGranularity(), log);
    }
}
