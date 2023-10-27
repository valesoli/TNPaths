package ar.edu.itba.algorithms.strategies.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import java.util.stream.Collectors;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.path.NodePath;

public class NodeIndexStrategy extends IndexStrategy {

    public NodeIndexStrategy(GraphDatabaseService db, String edgesLabel, Node startNode, Node endNode, String fromDate, String toDate, Long minLength, Long maxLength) {
        super(db, edgesLabel, startNode, endNode, fromDate, toDate, minLength, maxLength);
    }

    @Override
    public void subtractMetaIntervals() {
        Map<String, Object> indexNodeQueryParams = new HashMap<>();

        indexNodeQueryParams.put("from_date", fromDate);
        indexNodeQueryParams.put("to_date", toDate);
        indexNodeQueryParams.put("source_id", startNode.getProperty("id"));
        indexNodeQueryParams.put("destination_id", endNode.getProperty("id"));
        indexNodeQueryParams.put("min_length", minLength);
        indexNodeQueryParams.put("max_length", maxLength);

        String indexMetaNodeQuery = 
        "MATCH (indexMetaNode:INDEX:" + edgesLabel + ")\n"+
        "WHERE temporal.intersectsIntervalSplit(indexMetaNode.From, indexMetaNode.To, $from_date, $to_date) AND\n" +
        "indexMetaNode.MaxLength >= $min_length AND indexMetaNode.MaxLength <= $max_length\n" +
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
        Map<String, Object> indexNodeQueryParams = new HashMap<>();

        indexNodeQueryParams.put("from_date", fromDate);
        indexNodeQueryParams.put("to_date", toDate);
        indexNodeQueryParams.put("source_id", startNode.getProperty("id"));
        indexNodeQueryParams.put("destination_id", endNode.getProperty("id"));
        indexNodeQueryParams.put("min_length", minLength);
        indexNodeQueryParams.put("max_length", maxLength);
        
        String indexNodeQuery =
            "WITH apoc.date.format(timestamp(), 'ms', \"YYYY-MM-dd'T'HH:ss\") as nowString\n" +
            "WITH datetime(replace($from_date, ' ', 'T')) as searchFrom, datetime(replace(replace($to_date, 'Now', nowString), ' ', 'T')) as searchTo, nowString\n" +
            "MATCH (indexNode:INDEX)\n"+
            "WHERE ((datetime(replace(indexNode.From, ' ', 'T')) >= searchFrom) AND (datetime(replace(indexNode.From, ' ', 'T')) <= searchTo) OR\n" +
            "(datetime(replace(replace(indexNode.To, 'Now', nowString), ' ', 'T')) >= searchFrom) AND (datetime(replace(indexNode.From, ' ', 'T')) <= searchTo) OR\n" +
            "(datetime(replace(indexNode.From, ' ', 'T')) <=searchFrom) AND (datetime(replace(replace(indexNode.To, 'Now', nowString), ' ', 'T')) >= searchTo)) AND\n" +
            "indexNode.Source = $source_id AND indexNode.Destination = $destination_id AND\n" +
            "indexNode.Length >= $min_length AND indexNode.Length <= $max_length AND\n" +
            "EXISTS ((indexNode) - [:type] -> (:" + edgesLabel + "))\n" +
            "RETURN indexNode;";
        
        // log.info("Performed query:\n" + indexNodeQuery);

        Result indexNodesResult = db.execute(
                indexNodeQuery, indexNodeQueryParams
            );

        finalGranularity = Granularity.YEAR;
        
        Map<NodePath, List<Interval>> result = new HashMap<>();
        
        for (Map<String, Object> record : indexNodesResult.stream().collect(Collectors.toList())) {
            Node theNode = (Node) record.get("indexNode");
            // log.info("Got the node");
            // Remove indexed interval from whole query interval
            Interval nodeInterval = IntervalParser.fromStringLimits((String) theNode.getProperty("From"), (String) theNode.getProperty("To"));
            // notIndexed = notIndexed.subtract(nodeInterval);

            // TODO modularize and make this for all found Index nodes

            List<Long> intermediateNodes = 
                                                Arrays.stream((long[]) theNode.getProperty("Intermediate")).boxed().collect(Collectors.toList());
            // Get Relationships (why not store intervals in index?), then get their intervals
            intermediateNodes.add((long) theNode.getProperty("Destination"));
            // log.info("Got the intermediate nodes");

            NodePath nodePath = new NodePath();
            nodePath.addNodeToPath(startNode);

            // The following works because neo4j returns nodes matching "in" clause in the same order as matching parameters inside list

            Map<String, Object> queryParams = new HashMap<>();
        
            queryParams.put("intermediate_nodes", intermediateNodes);
            
            // log.info("Executing getNodes query");

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (o1:Object)\n")
                        .append("WHERE o1.id in $intermediate_nodes\n")
                        .append("RETURN o1");
            Result nodeResult = db.execute(queryBuilder.toString(), queryParams);

            nodeResult.stream().forEach(row -> {
                Node newNode = (Node) row.get("o1");
                nodePath.addNodeToPath(newNode);
            });

            // Add entry to result list, if already there, then add interval to existing intervalset
            if (result.containsKey(nodePath)) {
                result.get(nodePath).add(nodeInterval);
            } else {
                List<Interval> newList = new ArrayList<>();
                newList.add(nodeInterval);
                result.put(nodePath, newList);
            }

            finalGranularity = finalGranularity.getSmallerGranularity(nodeInterval.getGranularity());
        }
        return result;
    }


}
