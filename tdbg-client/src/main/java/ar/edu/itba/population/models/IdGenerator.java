package ar.edu.itba.population.models;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    private final AtomicInteger id;

    public IdGenerator(int initialValue) {
        this.id = new AtomicInteger(initialValue);
    }

    public int getNextId() {
        return id.getAndIncrement();
    }
}
