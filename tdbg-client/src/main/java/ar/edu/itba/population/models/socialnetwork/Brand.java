package ar.edu.itba.population.models.socialnetwork;

import ar.edu.itba.population.TimeInterval;
import ar.edu.itba.population.models.ObjectNode;
import ar.edu.itba.util.interval.Interval;

public class Brand extends ObjectNode {

    public static final String TITLE = "Brand";

    public Brand(int id, Interval interval) {
        super(id, interval);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
