package ar.edu.itba.graph.impl;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.config.constants.DirectionType;
import ar.edu.itba.graph.AdjacencyList;
import ar.edu.itba.graph.QueuedCachedGraph;
import ar.edu.itba.graph.weight.LightWeightMap;
import ar.edu.itba.graph.weight.WeightingMap;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.LinkedList;
import java.util.List;

public class PartialStoredGraph extends PartialGraph {

    private final QueuedCachedGraph cachedGraph = new QueuedCachedGraph();

    public PartialStoredGraph(
            GraphDatabaseService db,
            String nodeLabel,
            String edgeLabel,
            String nodeInterval,
            String edgeInterval,
            DirectionType directionType,
            Interval betweenInterval,
            boolean pruneNotBetween) {
        super(db, nodeLabel, edgeLabel, nodeInterval, edgeInterval, directionType, betweenInterval, pruneNotBetween);
    }


    @Override
    public List<Pair<List<Interval>, Long>> getRelationshipsFromNode(Long source) {
        if (cachedGraph.containsNode(source)) {
            return this.cachedGraph.getRelationshipsFromStructures(source);
        }
        List<Pair<List<Interval>, Long>> list = super.getRelationshipsFromNode(source);
        this.cachedGraph.setEdgesFromSource(source, list);
        return list;
    }

    @Override
    public List<Interval> getRelationshipsFromNodeToOther(Long source, Long target) {
        if (cachedGraph.containsNode(source)){
            return this.cachedGraph.fromSourceToTarget(source, target);
        }
        List<Interval> list = super.getRelationshipsFromNodeToOther(source, target);
        this.cachedGraph.setEdgesFromSourceAndTarget(source, target, list);
        return list;
    }

}
