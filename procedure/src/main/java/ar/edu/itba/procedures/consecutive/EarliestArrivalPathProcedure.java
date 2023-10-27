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

import java.util.*;
import java.util.stream.*;

@SuppressWarnings("unused")
public class EarliestArrivalPathProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="consecutive.earliest")
    @Description("Get the path with the earliest interval.")
    public Stream<TemporalPathRecord> earliest(
            @Name("initial node") Node initialNode,
            @Name("ending node") Node endingNode,
            @Name("consecutive difference") Long difference,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        log.info("Initializing coexisting graph algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        PathStrategy<Long> strategy = new ComparatorPruneConsecutiveStrategy(
                log,
                (n1, n2) -> n2.getTime().compareTo(n1.getTime()),// Comparator.comparingLong(TimeNode<Long>::getTime).reversed(),
                (solution, other) -> solution.getTime() < other.getTime(),
                difference,
                (n1, n2) -> {}
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
