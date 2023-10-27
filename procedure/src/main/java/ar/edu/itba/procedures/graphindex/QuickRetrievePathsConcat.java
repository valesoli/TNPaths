package ar.edu.itba.procedures.graphindex;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.path.IdOnlyNodePath;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.records.BooleanRecord;
import ar.edu.itba.records.TemporalPathRecord;

public class QuickRetrievePathsConcat {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value = "graphindex.quickRetrievePathsConcat")
    @Description("Concatenate indexed paths using BFS strategy")
    public Stream<TemporalPathRecord> retrievePathsBFSConcat(
    		@Name("startNode") Node startNode,
            @Name("endNode") Node endNode, 
            @Name("minLength") Long minLength, 
            @Name("maxLength") Long maxLength,
            @Name("configuration") Map<String, Object> config) {
        String searchIntervalStr = (String) config.get(ConfigurationFieldNames.BETWEEN);
        Interval searchInterval = IntervalParser.fromString(searchIntervalStr);
        String relationship = (String) config.get(ConfigurationFieldNames.EDGE_LABEL);


        Stopwatch timer = Stopwatch.createStarted();

        IntervalSet notIndexed = new IntervalSet(searchInterval);

        String indexMetaNodeQuery = 
        "MATCH (indexMetaNode:INDEX:META:" + relationship + ")\n"+
        "WHERE temporal.intersectsInterval('" + searchIntervalStr + "', indexMetaNode.From, indexMetaNode.To)\n" +
        "RETURN indexMetaNode";

        // log.info("Executing metaNodeQuery for retrieve " + indexMetaNodeQuery);

        // Result indexMetaNodesResult = db.execute(indexMetaNodeQuery);


        // for(Map<String, Object> record : indexMetaNodesResult.stream().collect(Collectors.toList())) {
        //     Node theNode = (Node) record.get("indexMetaNode");
        //     Interval indexedInterval = IntervalParser.fromStringLimits((String) theNode.getProperty("From"), (String) theNode.getProperty("To"));
        //     // log.info(notIndexed.toString() + " minus " + indexedInterval.toString() );
        //     notIndexed = notIndexed.subtract(indexedInterval);
        //     // log.info("equals " + notIndexed.toString());
        // }


        List<IntervalNodePairPath> resultList = new ArrayList<IntervalNodePairPath>();

        Map<NodePath, List<Interval>> result = new HashMap<>();

        for (Long length = minLength; length <= maxLength; length++) {
            // System.out.println("Starting algo");
            long halfLength = length / 2 - 1;
            long parity = length % 2;

            StringBuilder indexPrunableInfoQuery;
            // trying to speed it up, make it tidier l8r
            if (length > 3) {

                indexPrunableInfoQuery = new StringBuilder().append("MATCH p = (i1:INDEX:")
                .append(relationship).append(") - [:concat*").append(halfLength).append("] -> (i2:INDEX:")
                .append(relationship).append(")\n");
                if (parity == 1)
                indexPrunableInfoQuery.append("MATCH (u1:Object) - [r:").append(relationship)
                .append("] -> (u2:Object)");
                indexPrunableInfoQuery.append(" WHERE i1.Source = ").append(startNode.getProperty("id"));
                if (endNode != null) {
                    if (parity == 1)
                    indexPrunableInfoQuery.append(" AND i2.Destination = u1.id AND u2.id = ")
                    .append(endNode.getProperty("id"));
                    else
                    indexPrunableInfoQuery.append(" AND i2.Destination = ").append(endNode.getProperty("id"));
                } else if (parity == 1) {
                    indexPrunableInfoQuery.append(" AND i2.Destination = u1.id");
                }
                indexPrunableInfoQuery.append("\n RETURN relationships(p) as theRels");
                if (parity == 1)
                indexPrunableInfoQuery.append(", r as finalRel\n");
                // log.info("All valid paths query " + indexPrunableInfoQuery.toString());
                Result results = db.execute(indexPrunableInfoQuery.toString());
                
                results.stream().forEach(row -> {
                    ArrayList<Relationship> theRels = (ArrayList<Relationship>) row.get("theRels");

                Node iNode = theRels.get(0).getStartNode();
                    // >>>>>
                // IntervalNodePairPath first = new IntervalNodePairPath((Long) iNode.getProperty("Source"),
                //         new IntervalSet(IntervalParser.fromString((String) theRels.get(0).getProperty("interval"))),
                //         0L);
                //         IntervalNodePairPath current = first;
                // first = new IntervalNodePairPath((Long) iNode.getProperty("Intermediate"),
                // new IntervalSet(IntervalParser.fromString((String) theRels.get(0).getProperty("interval"))),
                //         1L);
                //         first.setPrevious(current);
                // current = first;
                // first = new IntervalNodePairPath((Long) iNode.getProperty("Destination"),
                // new IntervalSet(IntervalParser.fromString((String) theRels.get(0).getProperty("interval"))),
                // 2L);
                // first.setPrevious(current);
                // current = first;
                /// <<<<<
                IdOnlyNodePath pathIds = new IdOnlyNodePath();
                pathIds.addNodeIdToPath((Long) iNode.getProperty("Source"));
                pathIds.addNodeIdToPath((Long) iNode.getProperty("Intermediate"));
                pathIds.addNodeIdToPath((Long) iNode.getProperty("Destination"));
                IntervalSet pathIntervals = new IntervalSet(IntervalParser.fromString((String) theRels.get(0).getProperty("interval")));
                /// ======
                boolean discarded = false;
                for (Relationship rel : theRels) {
                    Interval relationshipInterval = IntervalParser.fromString((String) rel.getProperty("interval"));
                    
                    if (!relationshipInterval.intersection(searchInterval).isPresent()) {
                        discarded = true;
                        break;
                    }
                    
                    /// >>>>>>>
                    // TODO (not urgent) check for final granularity
                    
                    // IntervalSet intersection = current.getIntervalSet().intersection(relationshipInterval);

                    // if (intersection.isEmpty()) {
                    //     discarded = true;
                    //     break;
                    // }
                    // // This is repetitive but should run faster
                    // Long intermediate = (Long) rel.getEndNode().getProperty("Intermediate");
                    // Long destination = (Long) rel.getEndNode().getProperty("Destination");

                    // if (current.isInFullPath(intermediate) || current.isInFullPath(destination)) {
                    //     discarded = true;
                    //     break;
                    // }

                    // IntervalNodePairPath path1 = new IntervalNodePairPath(
                    //         intermediate, null, current.getLength() + 1);
                    //         path1.setPrevious(current);
                    // current = path1;
                    // path1 = new IntervalNodePairPath(destination, intersection,
                    // current.getLength() + 1);
                    // path1.setPrevious(current);
                    // current = path1;

                    /// <<<<<<<<<<<
                    
                    pathIntervals = pathIntervals.intersection(relationshipInterval);
                    if (pathIntervals.isEmpty()) {
                        discarded = true;
                        break;
                    }
                    Long intermediate = (Long) rel.getEndNode().getProperty("Intermediate");
                    Long destination = (Long) rel.getEndNode().getProperty("Destination");            
                    if (pathIds.containsId(intermediate) || pathIds.containsId(destination)) {
                        discarded = true;
                        break;
                    }
                    pathIds.addNodeIdToPath(intermediate);
                    if (endNode == null || destination != endNode.getProperty("id"))
                        pathIds.addNodeIdToPath(destination);
                    /// ===========
                    else if (endNode != null && destination.equals((Long) endNode.getProperty("id"))) {
                        pathIds.addNodeIdToPath((Long) endNode.getProperty("id"));
                    }
                }
                // System.out.println("Last rel");

                if (parity == 1) {
                    Relationship finalRel = (Relationship) row.get("finalRel");

                    String[] relationshipIntervalStrings = (String[]) finalRel.getProperty("interval"); // TODO handle
                    // exceptions
                    
                    List<Interval> relationshipIntervals = IntervalParser
                    .fromStringArrayToIntervals(relationshipIntervalStrings);
                    
                    /// >>>>>>>>>>>
                    // IntervalSet intersection = new IntervalSet(relationshipIntervals)
                    // .intersection(current.getIntervalSet());
                    
                    // if (intersection.intersection(searchInterval).isEmpty()) {
                    //     discarded = true;
                    // } else {
                    //     IntervalNodePairPath path = new IntervalNodePairPath(
                    //         (Long) finalRel.getEndNode().getProperty("id"), intersection, current.getLength() + 1);
                    //         path.setPrevious(current);
                    //     current = path;
                    // }
                    /// <<<<<<<<<<<<<
                    pathIntervals = pathIntervals.intersection(new IntervalSet(relationshipIntervals));
                    if (pathIntervals.intersection(searchInterval).isEmpty()) {
                        discarded = true;
                    } else if (endNode == null) {
                        pathIds.addNodeIdToPath((Long) finalRel.getEndNode().getProperty("id"));
                    } else if (endNode != null ) {
                        pathIds.addNodeIdToPath((Long) endNode.getProperty("id"));
                    }
                    /// =============

                }
                
                if (!discarded)
                    // resultList.add(current);
                    result.put(pathIds, pathIntervals.getIntervals());
            });
        }
            else {

                    indexPrunableInfoQuery = new StringBuilder().append("MATCH (i1:INDEX:")
                    .append(relationship).append(") \n");
                    if (parity == 1)
                    indexPrunableInfoQuery.append("MATCH (u1:Object) - [r:").append(relationship)
                    .append("] -> (u2:Object)");
                    indexPrunableInfoQuery.append("WHERE i1.Source = ").append(startNode.getProperty("id"));
                    if (endNode != null) {
                        if (parity == 1)
                        indexPrunableInfoQuery.append(" AND i1.Destination = u1.id AND u2.id = ")
                        .append(endNode.getProperty("id"));
                        else
                        indexPrunableInfoQuery.append(" AND i1.Destination = ").append(endNode.getProperty("id"));
                    } else if (parity == 1) {
                        indexPrunableInfoQuery.append(" AND i1.Destination = u1.id ");
                    }
                    indexPrunableInfoQuery.append("\n RETURN i1 as iNode");
                    if (parity == 1)
                    indexPrunableInfoQuery.append(", r as finalRel\n");
                    // log.info("All valid paths query " + indexPrunableInfoQuery.toString());
                    Result results = db.execute(indexPrunableInfoQuery.toString());
                    results.stream().forEach(row -> {
                        Node iNode = (Node) row.get("iNode");
                        /// >>>>>>>>>>>>
                        // IntervalNodePairPath first = new IntervalNodePairPath((Long) iNode.getProperty("Source"),
                        //         null,
                        //         0L);
                        // IntervalNodePairPath current = first;
                        // first = new IntervalNodePairPath((Long) iNode.getProperty("Intermediate"),
                        //         null,
                        //         1L);
                        // first.setPrevious(current);
                        // current = first;
                        // first = new IntervalNodePairPath((Long) iNode.getProperty("Destination"),
                        // new IntervalSet(IntervalParser.fromStringLimits((String) iNode.getProperty("From"), (String) iNode.getProperty("To"))),
                        // 2L);
                        // first.setPrevious(current);
                        // current = first;
                        // boolean discarded = current.getIntervalSet().intersection(searchInterval).isEmpty();
                        /// <<<<<<<<<<<<
                        IdOnlyNodePath pathIds = new IdOnlyNodePath();
                        pathIds.addNodeIdToPath((Long) iNode.getProperty("Source"));
                        pathIds.addNodeIdToPath((Long) iNode.getProperty("Intermediate"));
                        if (parity == 1 || endNode == null)
                            pathIds.addNodeIdToPath((Long) iNode.getProperty("Destination"));
                        else if (parity == 0 && endNode != null) //sry last minute change
                            pathIds.addNodeIdToPath((Long) endNode.getProperty("id"));
                        IntervalSet pathIntervals = new IntervalSet(IntervalParser.fromStringLimits((String) iNode.getProperty("From"), (String) iNode.getProperty("To")));
                        boolean discarded = pathIntervals.intersection(searchInterval).isEmpty();
                        /// ============
                        if (parity == 1) {
                            Relationship finalRel = (Relationship) row.get("finalRel");
        
                            String[] relationshipIntervalStrings = (String[]) finalRel.getProperty("interval"); // TODO handle
                            // exceptions
                            
                            List<Interval> relationshipIntervals = IntervalParser
                            .fromStringArrayToIntervals(relationshipIntervalStrings);
                            /// >>>>>>>>>>>
                            
                            // IntervalSet intersection = new IntervalSet(relationshipIntervals)
                            // .intersection(current.getIntervalSet());
                            
                            // if (intersection.intersection(searchInterval).isEmpty()) {
                            //     discarded = true;
                            // } else {
                            //     IntervalNodePairPath path = new IntervalNodePairPath(
                            //         (Long) finalRel.getEndNode().getProperty("id"), intersection, current.getLength() + 1);
                            //         path.setPrevious(current);
                            //     current = path;
                            // }
                            /// <<<<<<<<<<<<<
                            pathIntervals = pathIntervals.intersection(new IntervalSet(relationshipIntervals));
                            if (pathIntervals.intersection(searchInterval).isEmpty()) {
                                discarded = true;
                            } else if (endNode == null) {
                                pathIds.addNodeIdToPath((Long) finalRel.getEndNode().getProperty("id"));
                            } else if (endNode != null ) {
                                pathIds.addNodeIdToPath((Long) endNode.getProperty("id"));
                            }
                            /// =============
                        }
                        if (!discarded)
                            // resultList.add(current);
                            result.put(pathIds, pathIntervals.getIntervals());
                    });  
                }
            }

        timer.stop();
        log.info(String.format("QuickRetrievePathsConcat algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));

        // AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity> map = RetrieverUtils.addNotIndexedIfAny(notIndexed,
        //     new IntervalSet(),
        //     result,
        //     (Long) startNode.getProperty("id"),
        //     endNode == null ? null : (Long) endNode.getProperty("id"),
        //     relationship,
        //     minLength,
        //     maxLength,
        //     Granularity.YEAR,
        //     db,
        //     log);
        return TemporalPathRecord.getRecordsFromSolutionMap(result, db, Granularity.DATE);
    }

    @Procedure(value="graphindex.coTemporalPathsExist")
    @Description("Return the nodes and the intervals that complies with the between condition.")
    public Stream<BooleanRecord> coTemporalPathsIndexExist (
        @Name("node query") Node startNode,
        @Name("ending node") Node endNode,
        @Name("minimum length") Long minLength,
        @Name("maximum length") Long maxLength,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        if (minLength <= 0 || maxLength < minLength) {
            throw new IllegalArgumentException(
                "The minimum value cannot be 0 nor can the maximum value be lesser than the minimum.");
        }
        
        Map<String, Object> indexNodeQueryParams = new HashMap<>();

        String searchIntervalStr = (String) configuration.get(ConfigurationFieldNames.BETWEEN);
        Interval searchInterval = IntervalParser.fromString(searchIntervalStr);
        String relationship = (String) configuration.get(ConfigurationFieldNames.EDGE_LABEL);

        // String indexNodeQuery =
        //     "MATCH (indexNode:INDEX)\n"+
        //     "WHERE ((indexNode.From >= $from_date) AND (indexNode.From <= $to_date) OR\n" +
        //     "(indexNode.To >= $from_date) AND (indexNode.From <= $to_date) OR\n" +
        //     "(indexNode.From <=$from_date) AND (indexNode.To >= $to_date)) AND\n" +
        //     "indexNode.Source = $source_id AND indexNode.Destination = $destination_id AND\n" +
        //     "indexNode.Length >= $min_length AND indexNode.Length <= $max_length AND\n" +
        //     "EXISTS ((indexNode) - [:type] -> (:" + configuration.get("edgesLabel") + "))\n" +
        //     "RETURN indexNode;";
        
        // String indexNodeQuery = 
        //     "WITH apoc.date.format(timestamp(), 'ms', \"YYYY-MM-dd'T'HH:ss\") as nowString\n" +
        //     "WITH datetime(replace($from_date, ' ', 'T')) as searchFrom, datetime(replace(replace($to_date, 'now', nowstring), ' ', 'T')) as searchTo, nowString\n" +    
        //     "MATCH (indexNode:INDEX:META:" + configuration.get("edgesLabel") + ")\n" + 
        //     "WHERE (datetime(replace(indexNode.From, ' ', 'T')) >= searchFrom) AND (datetime(replace(indexNode.From, ' ', 'T')) <= searchTo) AND\n" +
        //     "datetime(replace(replace(indexNode.To, 'Now', nowString), ' ', 'T')) >= searchFrom AND (datetime(replace(replace(indexNode.To, 'Now', nowString), ' ', 'T')) <= searchTo) AND\n" +
        //     "indexNode.MaxLength >= $min_length\n" +
        //     "RETURN count(indexNode) as indexNodeCount";

        String indexMetaNodeQuery = 
        "MATCH (indexMetaNode:INDEX:META:" + relationship + ")\n"+
        "WHERE temporal.containsInterval('" + searchIntervalStr + "', indexMetaNode.From, indexMetaNode.To)\n" +
        "RETURN count(indexMetaNode) as indexNodeCount";

        // log.info("Performed query:\n" + indexNodeQuery);

        Result indexNodesResult = db.execute(
                indexMetaNodeQuery
            );
        
        Long indexMetaNodeCount = (Long) indexNodesResult.stream().findFirst().get().get("indexNodeCount");
        // log.info("Index Meta Node Count: " + indexMetaNodeCount);
        // There exists meta Node, means there also exist path index nodes 
        return Stream.of(new BooleanRecord(indexMetaNodeCount > 0));

        // return Stream.of(new BooleanRecord(!indexNodesResult.stream().collect(Collectors.toList()).isEmpty()));
    }
}
