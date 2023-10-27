package ar.edu.itba.client;

import ar.edu.itba.population.Population;
import ar.edu.itba.population.models.airports.FlightModel;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class FlightsConfigurationParser implements PopulationConfigurationParser {

    private int cityCount = 10;
    private int outgoingFlightsPerAirport = 3;
    private int flightsPerDestination = 3;

    @Override
    public Population parse(PropertiesConfiguration configuration) {
        cityCount = configuration.getInt("flights.cityCount", cityCount);
        outgoingFlightsPerAirport = configuration.getInt("flights.outgoingFlightsPerAirport", outgoingFlightsPerAirport);
        flightsPerDestination = configuration.getInt("flights.flightsPerDestination", flightsPerDestination);
        return build();
    }

    private Population build() {
        return new FlightModel.Builder(cityCount)
                .outgoingFlightsPerAirport(outgoingFlightsPerAirport)
                .flightsPerDestination(flightsPerDestination)
                .build();
    }
}
