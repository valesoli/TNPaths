package ar.edu.itba.population.models;

import ar.edu.itba.util.interval.Interval;

import java.util.Map;

public abstract class ObjectNode extends Node {

    public final static String LABEL = "Object";

    public ObjectNode(int id, Interval interval) {
        super(id, interval);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> asMap = super.toMap();
        asMap.put("title", getTitle());
        return asMap;
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    public abstract String getTitle();
}
