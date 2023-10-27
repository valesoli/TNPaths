package ar.edu.itba.procedures.temporal;

import ar.edu.itba.algorithms.operations.Between;
import ar.edu.itba.algorithms.utils.EntitySerializer;
import ar.edu.itba.algorithms.utils.ReturnFilter;
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
public class BetweenProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    private static final String name = "temporal.between";

    @Procedure(value = name)
    @Description("Return the nodes and the intervals that complies with the between condition.")
    public Stream<RowRecord> between(
            @Name("Query") String substringQuery,
            @Name("Between Interval") String betweenInterval,
            @Name("Return") Map<String, Object> returns) {
        log.info("Initializing between procedure algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Result result = db.execute(substringQuery);

        Interval i = IntervalParser.fromString(betweenInterval);
        Between between = new Between(i);

        Stream<RowRecord> recordStream = result.stream().filter(record -> {
            for (String columnName : record.keySet()) {
                Object o = record.get(columnName);

                if (o instanceof Entity) {
                    Entity entity = (Entity) record.get(columnName);
                    if (!checkIntersection(between, entity)) {
                        return false;
                    }
                }
                else if (o instanceof List) {
                    List<Entity> entities = (List<Entity>) record.get(columnName);
                    for (Entity entity : entities) {
                        if (!checkIntersection(between, entity)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }).map(EntitySerializer::serialize)
          .map(record -> new ReturnFilter(returns).apply(record)).map(RowRecord::new);

        timer.stop();
        log.info(String.format("Algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        return recordStream;
    }

    /**
     * Checks if there is intersection between an interval and the edges of an entity
     * @param between between operator
     * @param entity an entity
     * @return true if at least one interval intersected
     */
    private boolean checkIntersection(Between between, Entity entity) {
        List<Interval> intervals = IntervalParser.entityToIntervals(entity);
        boolean oneIntervalIntersected = false;
        for (Interval edgeInterval : intervals) {
            if (between.shouldApply(edgeInterval)) {
                oneIntervalIntersected = true;
                break;
            }
        }
        return oneIntervalIntersected;
    }

    public static String getName() {
        return name;
    }

}
