package ar.edu.itba.population.models.cpath;

public class CPathGenerationConfiguration {

    private final int minimumLengthOfPath;
    private final int numberOfPaths;

    public CPathGenerationConfiguration(int numberOfPaths, int minimumLengthOfPath) {

        if (numberOfPaths < 0 || minimumLengthOfPath <= 0) {
            throw new IllegalArgumentException("Number of paths and minimum length must be positive");
        }

        this.numberOfPaths = numberOfPaths;
        this.minimumLengthOfPath = minimumLengthOfPath;
    }

    public int getMinimumLengthOfPath() {
        return minimumLengthOfPath;
    }

    public int getNumberOfPaths() {
        return numberOfPaths;
    }

    public int nodesToGenerate() {
        return  (1 + minimumLengthOfPath) * numberOfPaths;
    }
}
