package ar.edu.itba.procedures;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.records.NodeIntervalRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ParallelPeriodProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="coexisting.parallel")
    public Stream<NodeIntervalRecord> parallel(
            @Name("initial node") Node initialNode,
            @Name("interval") String intervalStr,
            @Name("relationship") String relationship) {
        log.info("Initializing parallel period algorithm.");

        Stopwatch timer = Stopwatch.createStarted();

        Interval interval = IntervalParser.fromString(intervalStr);
        List<NodeIntervalRecord> records = new LinkedList<>();
        for (Relationship r : initialNode.getRelationships(RelationshipType.withName(relationship))) {
            Node other = r.getOtherNode(initialNode);
            IntervalParser.entityToIntervals(r).forEach(
                    (i) -> i.intersection(interval).ifPresent(
                            (intersection) -> records.add(
                                    new NodeIntervalRecord(other, (Interval) intersection)
                            )
                    ));
        }

        timer.stop();
        log.info(String.format("Algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));

        return records.stream();
    }
}
