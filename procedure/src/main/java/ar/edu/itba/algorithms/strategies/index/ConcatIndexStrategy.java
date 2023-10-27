package ar.edu.itba.algorithms.strategies.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import java.util.stream.Collectors;

import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.graph.GraphUtils;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.path.NodePath;

public class ConcatIndexStrategy extends IndexStrategy{
    
    public ConcatIndexStrategy(GraphDatabaseService db, String edgesLabel, Node startNode, Node endNode, String fromDate, String toDate, Long minLength, Long maxLength) {
        super(db, edgesLabel, startNode, endNode, fromDate, toDate, minLength, maxLength);
    }

    public ConcatIndexStrategy(GraphDatabaseService db, String edgesLabel, Node startNode, String fromDate, String toDate, Long minLength, Long maxLength) {
        super(db, edgesLabel, startNode, fromDate, toDate, minLength, maxLength);
    }

    @Override
    public void subtractMetaIntervals() {
        Map<String, Object> indexNodeQueryParams = new HashMap<>();

        indexNodeQueryParams.put("from_date", fromDate);
        indexNodeQueryParams.put("to_date", toDate);
        indexNodeQueryParams.put("source_id", startNode.getProperty("id"));

        String indexMetaNodeQuery = 
        "MATCH (indexMetaNode:INDEX:" + edgesLabel + ")\n"+
        "WHERE temporal.intersectsIntervalSplit(indexMetaNode.From, indexMetaNode.To, $from_date, $to_date)\n" +
        "RETURN indexMetaNode";

        // log.info("Executing metaNodeQuery for retrieve " + indexMetaNodeQuery);

        Result indexMetaNodesResult = db.execute(indexMetaNodeQuery, indexNodeQueryParams);


        for(Map<String, Object> record : indexMetaNodesResult.stream().collect(Collectors.toList())) {
            Node theNode = (Node) record.get("indexMetaNode");
            Interval indexedInterval = IntervalParser.fromStringLimits((String) theNode.getProperty("From"), (String) theNode.getProperty("To"));
            // log.info(notIndexed.toString() + " minus " + indexedInterval.toString() );
            notIndexed = notIndexed.subtract(indexedInterval);
            // log.info("equals " + notIndexed.toString());
            indexedIntervals.union(indexedInterval);
        }
    }

    @Override
    public Map<NodePath, List<Interval>> getSolution() {
        // TODO do for all lengths
        
        StringBuilder pathIntervalQueryBuilder = new StringBuilder();
        
        for(long length = minLength; length <= maxLength; length ++) {

            
            long subPathCount = maxLength / 2;

            // Match statement
            for (int i = 0; i < subPathCount; i++) {
                pathIntervalQueryBuilder.append("MATCH (i" + i + ":INDEX) ");
            }
            if (maxLength % 2 != 0)
                pathIntervalQueryBuilder.append("MATCH (u1:Object)-[r:" + edgesLabel + "] -> (u2:Object)");

            pathIntervalQueryBuilder.append("\n");

            // Where

            // Source & Destination
            pathIntervalQueryBuilder.append("WHERE i0.Source = " + startNode.getProperty("id"));
            
            if (endNode != null) {
                if (maxLength % 2 == 0)
                    pathIntervalQueryBuilder.append(" AND i" + (subPathCount-1) + ".Destination = " + endNode.getProperty("id"));
                else
                pathIntervalQueryBuilder.append(" AND u2.id = " + endNode.getProperty("id") + "\n");
            }

            // Links
            for (int i = 0; i < subPathCount-1; i++) {
                pathIntervalQueryBuilder.append( " AND i" + i + ".Destination = i" + (i+1) + ".Source");
            }

            if (maxLength % 2 != 0)
                pathIntervalQueryBuilder.append(" AND i" + (subPathCount - 1) + ".Destination = u1.id");
            
            pathIntervalQueryBuilder.append("\n");
            // Lenght = 2

            for (int i = 0; i < subPathCount; i++) {
                pathIntervalQueryBuilder.append(" AND i" + i + ".Length = 2");
            }

            pathIntervalQueryBuilder.append("\n");

            // unwind relationship intervals

            if (maxLength % 2 != 0)
                pathIntervalQueryBuilder.append("UNWIND r.interval as ints\n");

            // return

            pathIntervalQueryBuilder.append("RETURN {path: [");

            for(int i = 0; i < subPathCount; i++) {
                pathIntervalQueryBuilder.append("i" + i + ".Source" + ", i" + i + ".Intermediate[0], ");
            }

            pathIntervalQueryBuilder.append("i" + (subPathCount-1) + ".Destination");
            
            if (maxLength % 2 != 0)
                pathIntervalQueryBuilder.append(", u2.id");

            pathIntervalQueryBuilder.append("],\ninterval: [");

            for (int i = 0; i < subPathCount; i++) {
                pathIntervalQueryBuilder.append("(i" + i + ".From + '—' + i" + i + ".To)");
                if (i < subPathCount - 1)
                    pathIntervalQueryBuilder.append(", ");
            }

            if (maxLength % 2 != 0)
                pathIntervalQueryBuilder.append(", ints");

            pathIntervalQueryBuilder.append("]} as pairs");
            
            if (length < maxLength)
                pathIntervalQueryBuilder.append("\nUNION ALL\n");
        }
        // log.info("retrieving paths\n" + pathIntervalQueryBuilder.toString());

        Result pathIntervalPairs = db.execute(pathIntervalQueryBuilder.toString()); 

        Map<NodePath, List<Interval>> result = new HashMap<>();

        finalGranularity = Granularity.YEAR;

        for (Map<String, Object> resultRow : pathIntervalPairs.stream().collect(Collectors.toList())) {
            Map<String, Object> pair = (Map<String, Object>) resultRow.get("pairs");
            ArrayList<Long> path = (ArrayList<Long>) pair.get("path");
            ArrayList<String> intervals = (ArrayList<String>) pair.get("interval");

            Interval pathInterval = IntervalParser.fromString(intervals.get(0));
            boolean validPath = true;
            for (String interval : intervals) {
                Optional<Interval> nextIntersection = pathInterval.intersection(IntervalParser.fromString(interval));
                if (!nextIntersection.isPresent()) {
                    validPath = false;
                    break;
                }
                pathInterval = nextIntersection.get();
            }
            validPath = validPath && pathInterval.isIntersecting(betweenInterval) && !GraphUtils.hasLoops(path);;
            if (!validPath) continue;
            
            // remove from not indexed

            NodePath nodePath = new NodePath();
            nodePath.addNodeToPath(startNode);

            // The following works because neo4j returns nodes matching "in" clause in the same order as matching parameters inside list

            Map<String, Object> queryParams = new HashMap<>();
        
            queryParams.put("intermediate_nodes", path.subList(1, (endNode != null) ? path.size()-1 : path.size()));
            
            // log.info("Executing getNodes query");

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("with $intermediate_nodes as ids\n")
                .append("unwind range(0, size(ids) - 1) as index\n")
                .append("MATCH (o1:Object {id:ids[index]})\n")
                .append("with index, o1\n")
                .append("order by index asc\n")
                .append("RETURN o1\n");
            Result nodeResult = db.execute(queryBuilder.toString(), queryParams);

            nodeResult.stream().forEach(row -> {
                Node newNode = (Node) row.get("o1");
                nodePath.addNodeToPath(newNode);
            });

            // Add entry to result list, if already there, then add interval to existing intervalset
            if (result.containsKey(nodePath)) {
                result.get(nodePath).add(pathInterval);
            } else {
                List<Interval> newList = new ArrayList<>();
                newList.add(pathInterval);
                result.put(nodePath, newList);
            }
            finalGranularity = finalGranularity.getSmallerGranularity(pathInterval.getGranularity());
            // notIndexed = notIndexed.subtract(pathInterval);
        }
        // log.info("all done");
        return result;
    }
}
