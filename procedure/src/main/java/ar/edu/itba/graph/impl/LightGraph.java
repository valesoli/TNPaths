package ar.edu.itba.graph.impl;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.config.constants.DirectionType;
import ar.edu.itba.graph.AdjacencyList;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.graph.weight.LightWeightMap;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LightGraph extends Graph {

    private final AdjacencyList adjacencyList = new AdjacencyList();
    private final LightWeightMap lightWeightMap = new LightWeightMap();
    private final DirectionType direction;

    public LightGraph(
            GraphDatabaseService db,
            String weightQuery,
            DirectionType direction,
            Interval betweenInterval,
            boolean pruneNotBetween) {
        super(betweenInterval, pruneNotBetween);
        this.direction = direction;
        db.execute(weightQuery).stream().forEach(
            this::addRows
        );
    }

    @Override
    public List<Pair<List<Interval>, Long>> getRelationshipsFromNode(Long source) {
        List<Pair<List<Interval>, Long>> relationships = new LinkedList<>();
        this.adjacencyList.getAdjacentNodes(source).forEach(
                (target) -> relationships.add(
                        Pair.of(this.lightWeightMap.weight(source, target), target)
                )
        );
        return relationships;
    }

    @Override
    public List<Interval> getRelationshipsFromNodeToOther(Long source, Long target) {
        return this.lightWeightMap.weight(source, target);
    }

    @Override
    public List<Interval> getIntervalsFromNode(Long nodeId) {
        return new LinkedList<>();
    }

    @Override
    public Granularity getGranularity() {
        return this.granularity;
    }

    private void addRows(Map<String, Object> row) {
        Long source = (Long) row.get("source");
        Long target = (Long) row.get("target");
        List<Interval> intervalSet = IntervalParser.fromStringArrayToIntervals((String[]) row.get("weight"));
        this.setGranularityFromList(intervalSet);
        intervalSet = this.filterByInterval(intervalSet);
        this.insertionStrategy(source, target, intervalSet);
    }

    private void insertionStrategy(Long source, Long target, List<Interval> intervals) {
        switch (this.direction) {
            case BOTH:
                this.adjacencyList.addNode(source, target);
                this.adjacencyList.addNode(target, source);
                this.lightWeightMap.put(source, target, intervals);
                this.lightWeightMap.put(target, source, intervals);
                break;
            case INCOMING:
                this.adjacencyList.addNode(target, source);
                this.lightWeightMap.put(target, source, intervals);
                break;
            case OUTGOING:
                this.adjacencyList.addNode(source, target);
                this.lightWeightMap.put(source, target, intervals);
                break;
        }
    }
}
