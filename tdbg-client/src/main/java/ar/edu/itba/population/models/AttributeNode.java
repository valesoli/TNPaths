package ar.edu.itba.population.models;


import java.util.Map;

import ar.edu.itba.util.interval.Interval;

public class AttributeNode extends Node {

    public final static String LABEL = "Attribute";
    private final String title;

    public AttributeNode(int id, Interval interval, String title) {
        super(id, interval);
        this.title = title;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("title", title);
        return map;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getLabel() {
        return LABEL;
    }
}
