package ar.edu.itba.procedures.graphindex;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.IndexIntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalPathPair;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.records.TemporalPathRecord;

public class BFSRetrievePaths {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="graphindex.retrievePathsBFSConcat")
    @Description("Concatenate indexed paths using BFS strategy")
    public Stream<TemporalPathRecord> retrievePathsBFSConcat(
        @Name("startNode") Node startNode,
        @Name("endNode") Node endNode,
        @Name("minLength") Long minLength,
        @Name("maxLength") Long maxLength,
        @Name("configuration") Map<String, Object> config
    ) {

        String searchIntervalStr = (String) config.get(ConfigurationFieldNames.BETWEEN);
        Interval searchInterval = IntervalParser.fromString(searchIntervalStr);
        String relationship = (String) config.get(ConfigurationFieldNames.EDGE_LABEL);

        IntervalSet notIndexed = new IntervalSet(searchInterval);

        String indexMetaNodeQuery = 
        "MATCH (indexMetaNode:INDEX:" + relationship + ")\n"+
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


        Stopwatch timer = Stopwatch.createStarted();

        Deque<IndexIntervalNodePairPath> frontier = new LinkedList<>();
        List<IntervalNodePairPath> solutions = new ArrayList<>();
        
        StringBuilder sourceIndexNodesQueryBuilder = new StringBuilder();

        sourceIndexNodesQueryBuilder
            .append("MATCH (i:INDEX:").append(relationship)
            .append(") WHERE i.Source = ").append(startNode.getProperty("id"))
            .append("\n    AND temporal.intersectsInterval('").append(searchIntervalStr).append("', i.From, i.To)\n")
            .append("RETURN i order by i.Source, i.Destination, i.Intermediate, i.From, i.To");
        
        // log.info("Source index nodes query:\n" + sourceIndexNodesQueryBuilder.toString());

        Result sourceIndexNodes = db.execute(sourceIndexNodesQueryBuilder.toString());
        
        frontier.addAll(sourceIndexNodes.stream().map(row -> {
            Node i = (Node) row.get("i");
            return IndexIntervalNodePairPath.fromTwoPath(
                (Long) i.getId(),
                (Long) i.getProperty("Source"),
                (Long) i.getProperty("Intermediate"),
                (Long) i.getProperty("Destination"),
                new IntervalSet(IntervalParser.fromStringLimits((String) i.getProperty("From"), (String) i.getProperty("To"))),
                0L
            );
        }).collect(Collectors.toList()));
        
        // log.info("First population of frontier");

        IndexIntervalNodePairPath u; 
        while (!frontier.isEmpty()) {
            u = frontier.pollFirst();
            // log.info("Path ending at " + u.getNode() + " with length " + u.getLength() + ". Comparing to " + endNode.getProperty("id"));
            if (u.getLength() >= minLength && (endNode == null || endNode.getProperty("id").equals(u.getNode()))) {
                solutions.add(u);
            }

            // Even-lengthed path, extend both ways
            if (u.getLength() % 2 == 0) {
                if (u.getLength() + 2 <= maxLength) {
                    Node iNode = db.getNodeById(u.getTgIndexNode());
                    for(Relationship rel : iNode.getRelationships(Direction.OUTGOING, RelationshipType.withName("concat"))) {
                        Interval relInterval = IntervalParser.fromString((String) rel.getProperty("interval"));
                        IntervalSet intersection = u.getIntervalSet().intersection(relInterval);
                        if (!intersection.intersection(searchInterval).isEmpty()) {
                            Node nextINode = rel.getEndNode();
                            Long intermediate = (Long) nextINode.getProperty("Intermediate");
                            Long destination = (Long) nextINode.getProperty("Destination");
                            if (!u.isInFullPath(intermediate) && !u.isInFullPath(destination))
                                frontier.add(
                                    u.extendWithTwoPath(nextINode.getId(),
                                                        intermediate,
                                                        destination,
                                                        intersection
                                                    )
                                );
                        }
                    }
                }
                if (u.getLength() + 1 >= minLength && u.getLength() + 1 <= maxLength) {
                    Node node = db.findNode(Label.label("Object"), "id", u.getNode());
                    for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelationshipType.withName(relationship))) {
                        IntervalSet relInterval = new IntervalSet(Arrays.stream((String[]) rel.getProperty("interval")).map(str -> IntervalParser.fromString(str)).collect(Collectors.toList()));
                        IntervalSet intersection = u.getIntervalSet().intersection(relInterval);
                        if (!intersection.intersection(searchInterval).isEmpty()) {
                            Long endId = (Long) rel.getEndNode().getProperty("id");
                            if (!u.isInFullPath(endId)) {
                                IndexIntervalNodePairPath path = new IndexIntervalNodePairPath(null, endId, intersection, u.getLength() + 1);
                                path.setPrevious(u);
                                frontier.add(
                                    path
                                );
                            }
                        }
                    }
                }
            }
            // Odd-lengthed path, do not extend
        }

        timer.stop();
        log.info(String.format("BFS Concat algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));


        // log.info("Collected " + solutions.size() + " solutions");
        // return TemporalPathRecord.getRecordsFromSolutionListByPropertyId(solutions, db, Granularity.YEAR, log);
        Map<NodePath, List<Interval>> solutionsMap = TemporalPathRecord.getSolutionsMapFromSolutionsList(solutions, db, Granularity.YEAR, log);
        return TemporalPathRecord.getRecordsFromSolutionMap(solutionsMap, db, Granularity.YEAR);
        // RetrieverUtils.addNotIndexedIfAny(notIndexed, new IntervalSet(), solutionsMap, startNode.getId(), endNode != null ? endNode.getId() : null,
        //  relationship, minLength, maxLength, Granularity.YEAR, db, log);
    }

    @Procedure(value="graphindex.retrievePathsBFSConcatOld")
    @Description("Concatenate indexed paths using BFS strategy")
    public Stream<TemporalPathRecord> retrievePathsBFSConcatOld(
        @Name("startNode") Node startNode,
        @Name("endNode") Node endNode,
        @Name("minLength") Long minLength,
        @Name("maxLength") Long maxLength,
        @Name("configuration") Map<String, Object> config
    ) {

        String searchIntervalStr = (String) config.get(ConfigurationFieldNames.BETWEEN);
        Interval searchInterval = IntervalParser.fromString(searchIntervalStr);
        String relationship = (String) config.get(ConfigurationFieldNames.EDGE_LABEL);

        IntervalSet notIndexed = new IntervalSet(searchInterval);

        String indexMetaNodeQuery = 
        "MATCH (indexMetaNode:INDEX:" + relationship + ")\n"+
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


        Stopwatch timer = Stopwatch.createStarted();

        Deque<IndexIntervalNodePairPath> frontier = new LinkedList<>();
        List<IntervalNodePairPath> solutions = new ArrayList<>();
        
        StringBuilder sourceIndexNodesQueryBuilder = new StringBuilder();

        sourceIndexNodesQueryBuilder
            .append("MATCH (i:INDEX:").append(relationship)
            .append(") WHERE i.Source = ").append(startNode.getProperty("id"))
            .append("\n    AND temporal.intersectsInterval('").append(searchIntervalStr).append("', i.From, i.To)\n")
            .append("RETURN i");
        
        // log.info("Source index nodes query:\n" + sourceIndexNodesQueryBuilder.toString());

        Result sourceIndexNodes = db.execute(sourceIndexNodesQueryBuilder.toString());
        
        frontier.addAll(sourceIndexNodes.stream().map(row -> {
            Node i = (Node) row.get("i");
            return IndexIntervalNodePairPath.fromTwoPath(
                (Long) i.getId(),
                (Long) i.getProperty("Source"),
                (Long) i.getProperty("Intermediate"),
                (Long) i.getProperty("Destination"),
                new IntervalSet(IntervalParser.fromStringLimits((String) i.getProperty("From"), (String) i.getProperty("To"))),
                0L
            );
        }).collect(Collectors.toList()));
        
        // log.info("First population of frontier");

        IndexIntervalNodePairPath u; 
        while (!frontier.isEmpty()) {
            u = frontier.pollFirst();
            // log.info("Path ending at " + u.getNode() + " with length " + u.getLength() + ". Comparing to " + endNode.getProperty("id"));
            if (u.getLength() >= minLength && (endNode == null || endNode.getProperty("id").equals(u.getNode()))) {
                solutions.add(u);
            }

            // Even-lengthed path, extend both ways
            if (u.getLength() % 2 == 0) {
                if (u.getLength() + 2 <= maxLength) {
                    Node iNode = db.getNodeById(u.getTgIndexNode());
                    for(Relationship rel : iNode.getRelationships(Direction.OUTGOING, RelationshipType.withName("concat"))) {
                        Interval relInterval = IntervalParser.fromString((String) rel.getProperty("interval"));
                        IntervalSet intersection = u.getIntervalSet().intersection(relInterval);
                        if (!intersection.intersection(searchInterval).isEmpty()) {
                            Node nextINode = rel.getEndNode();
                            Long intermediate = (Long) nextINode.getProperty("Intermediate");
                            Long destination = (Long) nextINode.getProperty("Destination");
                            if (!u.isInFullPath(intermediate) && !u.isInFullPath(destination))
                                frontier.add(
                                    u.extendWithTwoPath(nextINode.getId(),
                                                        intermediate,
                                                        destination,
                                                        intersection
                                                    )
                                );
                        }
                    }
                }
                if (u.getLength() + 1 >= minLength && u.getLength() + 1 <= maxLength) {
                    Node node = db.findNode(Label.label("Object"), "id", u.getNode());
                    for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelationshipType.withName(relationship))) {
                        IntervalSet relInterval = new IntervalSet(Arrays.stream((String[]) rel.getProperty("interval")).map(str -> IntervalParser.fromString(str)).collect(Collectors.toList()));
                        IntervalSet intersection = u.getIntervalSet().intersection(relInterval);
                        if (!intersection.intersection(searchInterval).isEmpty()) {
                            Long endId = (Long) rel.getEndNode().getProperty("id");
                            if (!u.isInFullPath(endId)) {
                                IndexIntervalNodePairPath path = new IndexIntervalNodePairPath(null, endId, intersection, u.getLength() + 1);
                                path.setPrevious(u);
                                frontier.add(
                                    path
                                );
                            }
                        }
                    }
                }
            }
            // Odd-lengthed path, do not extend
        }

        timer.stop();
        log.info(String.format("BFS Concat algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));


        // log.info("Collected " + solutions.size() + " solutions");
        return TemporalPathRecord.getRecordsFromSolutionListByPropertyId(solutions, db, Granularity.YEAR, log);
        // Map<NodePath, List<Interval>> solutionsMap = TemporalPathRecord.getSolutionsMapFromSolutionsList(solutions, db, Granularity.YEAR, log);
        // RetrieverUtils.addNotIndexedIfAny(notIndexed, new IntervalSet(), solutionsMap, startNode.getId(), endNode != null ? endNode.getId() : null,
        //  relationship, minLength, maxLength, Granularity.YEAR, db, log);
    }
}
