package ar.edu.itba.population.models.common;

import ar.edu.itba.population.TimeInterval;
import ar.edu.itba.population.models.ObjectNode;
import ar.edu.itba.util.interval.Interval;

public class City extends ObjectNode {

    public static final String TITLE = "City";

    public City(int id, Interval interval) {
        super(id, interval);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
