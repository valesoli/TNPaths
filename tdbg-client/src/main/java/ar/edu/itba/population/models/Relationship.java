package ar.edu.itba.population.models;

import ar.edu.itba.util.interval.Interval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Relationship {

    private final RelationshipType type;
    private final Node from;
    private final Node to;
    private List<Interval> interval;

    public Relationship(RelationshipType type, Node from, Node to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public Relationship(RelationshipType type, Node from, Node to, List<Interval> interval) {
        this(type, from, to);
        this.interval = interval;
    }

    public List<Interval> getIntervals() {
        return interval;
    }

    public void setIntervals(List<Interval> intervals) {
        this.interval = intervals;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("from", from.getId());
        map.put("fromLabel", from.getLabel());
        map.put("to", to.getId());
        map.put("toLabel", to.getLabel());
        map.put("type", type.toString());
        if (interval != null) {
            map.put("interval", interval.stream().map(Interval::toString).collect(Collectors.toList()));
        }
        return map;
    }

    public boolean hasSameEndpoints(Relationship other) {
        return this.from.getId() == other.from.getId() && this.to.getId() == other.to.getId();
    }
}
