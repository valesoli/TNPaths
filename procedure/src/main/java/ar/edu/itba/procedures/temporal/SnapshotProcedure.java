package ar.edu.itba.procedures.temporal;

import ar.edu.itba.algorithms.operations.Snapshot;
import ar.edu.itba.algorithms.utils.EntitySerializer;
import ar.edu.itba.algorithms.utils.ReturnFilter;
import ar.edu.itba.algorithms.utils.SnapshotIntervalFilter;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.records.RowRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class SnapshotProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    private static final String name = "temporal.snapshot";

    @Procedure(value = name)
    @Description("Return a non-temporal sub-graph that complies with the snapshot condition.")
    public Stream<RowRecord> snapshot(
            @Name("Query") String substringQuery,
            @Name("Snapshot Time") String snapshotTime,
            @Name("Return") Map<String, Object> returns) {
        log.info("Initializing snapshot procedure algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Result subQueryResult = db.execute(substringQuery);
        Snapshot snapshot = new Snapshot(snapshotTime);

        Stream<RowRecord> recordStream = subQueryResult.stream().filter(record -> {
            for (String columnName : record.keySet()) {

                Object o = record.get(columnName);

                if (o instanceof Entity) {
                    Entity entity = (Entity) record.get(columnName);
                    if (!checkIntersection(entity, snapshot)) {
                        return false;
                    }
                }
                else if (o instanceof List) {
                    List<Entity> entities = (List<Entity>) record.get(columnName);
                    for (Entity entity : entities) {
                        if (!checkIntersection(entity, snapshot)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        })
          .map(EntitySerializer::serialize)
          .map(SnapshotIntervalFilter::filter)
          .map(record -> new ReturnFilter(returns).apply(record))
          .map(RowRecord::new);

        timer.stop();
        log.info(String.format("Algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        return recordStream;
    }

    private boolean checkIntersection(Entity entity, Snapshot snapshot) {
        List<Interval> intervals = IntervalParser.entityToIntervals(entity);
        boolean oneIntervalIntersected = false;
        for (Interval interval : intervals) {
            if (snapshot.shouldApply(interval)) {
                oneIntervalIntersected = true;
                break;
            }
        }
        return oneIntervalIntersected;
    }

    public static String getName(){
        return name;
    }
}
