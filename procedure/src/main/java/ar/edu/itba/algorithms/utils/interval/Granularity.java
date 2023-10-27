package ar.edu.itba.algorithms.utils.interval;

public enum Granularity {
    YEAR(0),
    YEAR_MONTH(1),
    DATE(2),
    DATETIME(3);

    private final int value;

    Granularity(int value){
        this.value = value;
    }
    public Granularity getSmallerGranularity(Granularity other) {
        if (this.value > other.value) {
            return this;
        }
        return other;
    }
}
