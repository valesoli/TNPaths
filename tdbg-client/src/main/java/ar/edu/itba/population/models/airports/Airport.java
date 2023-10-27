package ar.edu.itba.population.models.airports;

import ar.edu.itba.population.TimeInterval;
import ar.edu.itba.population.models.ObjectNode;
import ar.edu.itba.population.models.common.City;
import ar.edu.itba.util.interval.Interval;

public class Airport extends ObjectNode {

    public static final String TITLE = "Airport";
    private String name;
    private City city;

    public Airport(int id, String name, City city, Interval interval) {
        super(id, interval);
        this.name = name;
        this.city = city;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
