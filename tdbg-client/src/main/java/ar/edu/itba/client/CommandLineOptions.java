package ar.edu.itba.client;

import ar.edu.itba.population.PopulationModel;

public class CommandLineOptions {

    private final String configurationFile;
    private final boolean populate;
    private final PopulationModel populationModel;

    public CommandLineOptions(String configurationFile, boolean populate, PopulationModel populationModel) {
        this.configurationFile = configurationFile;
        this.populate = populate;
        this.populationModel = populationModel;
    }

    public boolean isPopulateMode() {
        return populate;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public PopulationModel getPopulationModel() {
        return populationModel;
    }
}
