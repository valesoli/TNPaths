package ar.edu.itba.procedures.graphindex;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.records.TemporalPathRecord;
import ar.edu.itba.records.utils.NodeSerialization;

import org.neo4j.graphdb.Result;

public class RetrieverUtils {
    
    private RetrieverUtils () {}

    public static Result getNotIndexedPaths(IntervalSet notIndexed, Long startNodeId,
        Long endNodeId, String edgesLabel, Long minLength, Long maxLength,
        GraphDatabaseService db, Log log) {
        // log.info("Not indexed: " + notIndexed.getIntervals().toString());

        StringBuilder notIndexedQuery = new StringBuilder();

        String uname1, uname2;
        for (int i = 0; i < notIndexed.getIntervals().size(); i++) {
            uname1 = "u" + i + "1";
            uname2 = "u" + i + "2";
            if (endNodeId != null) {
                notIndexedQuery.append("MATCH (").append(uname1).append(":Object) MATCH (").append(uname2).append(":Object)\n")
                .append("WHERE ").append(uname1).append(".id = ").append(startNodeId)
                    .append(" AND ").append(uname2).append(".id = ").append(endNodeId).append("\n");
            }
            else {
                notIndexedQuery.append("MATCH (").append(uname1).append(":Object)\n")
                .append("WHERE ").append(uname1).append(".id = ").append(startNodeId);
            }
            notIndexedQuery.append("\nCALL coexisting.");
            
            if (endNodeId != null) notIndexedQuery.append("quickCoTemporalPaths(").append(uname1).append(", ").append(uname2).append(", ");
            else notIndexedQuery.append("sourceOnlyQuickCoTemporalPaths(").append(uname1).append(", ");

            notIndexedQuery
                        .append(minLength).append(", ").append(maxLength).append(", {edgesLabel: '").append(edgesLabel)
                        .append("', between:'").append(notIndexed.getIntervals().get(i)).append("', direction: 'outgoing'})\n")
            .append("YIELD path, interval RETURN path, interval\n");
            if (i != notIndexed.getIntervals().size() - 1)
                notIndexedQuery.append("UNION ALL\n");
        }

        log.info("Executing not Indexed query:\n" + notIndexedQuery.toString());

        Result notIndexedQueryResult = db.execute(notIndexedQuery.toString());

        // log.info("Merging not indexed results\n");
        return notIndexedQueryResult;
    }

    public static AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity> addNotIndexedIfAny(
            IntervalSet notIndexed, IntervalSet indexedIntervals, Map<NodePath, List<Interval>> indexedResults,
            Long startNodeId, Long endNodeId, String edgesLabel, Long minLength,
            Long maxLength, Granularity finalGranularity, GraphDatabaseService db, Log log) {
        
        // TODO correct order of if-else for neatness and efficiency
        // log.info("Not indexed: " + notIndexed.getIntervals().toString());

        if (!notIndexed.isEmpty()) {
            List<Map<String, Object>> notIndexedQueryResult = getNotIndexedPaths(notIndexed, startNodeId, endNodeId, edgesLabel, minLength, maxLength, db, log)
                                                                .stream().collect(Collectors.toList());
            Map<NodePath, List<Interval>> deserializedNotIndexedQueryResult = NodeSerialization.deserializePathIntervalRows(notIndexedQueryResult, endNodeId == null, db);

            log.info("deserializedNotIndexedQueryResult");
            for (NodePath key : deserializedNotIndexedQueryResult.keySet()) {
                log.info(key.toString() + ": " + deserializedNotIndexedQueryResult.get(key));
            }
            log.info("indexedResults" + "[" + indexedResults.keySet().size() + "]");
            for (NodePath key : indexedResults.keySet()) {
                log.info(key.toString() + ": " + indexedResults.get(key));
            }

            // Now merge the two Maps
            for (NodePath key : deserializedNotIndexedQueryResult.keySet()) {
                // TODO consider using set of intervals in the future

                // IntervalSet intervals = new IntervalSet(deserializedNotIndexedQueryResult.get(key));

                // if (!intervals.intersection(indexedIntervals).isEmpty())
                //     continue;

                if (indexedResults.containsKey(key)) {
                    List<Interval> indexedResultInterval = indexedResults.get(key);
                    deserializedNotIndexedQueryResult.get(key).forEach(newInterval -> {
                        // This is necessary because interval equality is not enough. Intervals may not be equal but still one could be discarded
                        boolean include = true;
                        for (Interval interval : indexedResultInterval) {
                            include = include & !interval.containsInterval(newInterval);
                        }
                        if (include) indexedResultInterval.add(newInterval);
                    });
                } else
                    indexedResults.put(key, deserializedNotIndexedQueryResult.get(key));
            }
        }

        return new AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity>(indexedResults, finalGranularity);
    }
}
