package ar.edu.itba.client;

import ar.edu.itba.population.Population;
import org.apache.commons.configuration2.PropertiesConfiguration;

public interface PopulationConfigurationParser {

    Population parse(PropertiesConfiguration configuration);

}
