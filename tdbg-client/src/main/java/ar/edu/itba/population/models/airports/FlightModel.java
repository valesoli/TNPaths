package ar.edu.itba.population.models.airports;

import ar.edu.itba.population.DataFactory;
import ar.edu.itba.population.DateTimeInterval;
import ar.edu.itba.population.Population;
import ar.edu.itba.population.TimeInterval;
import ar.edu.itba.population.models.common.City;
import ar.edu.itba.util.Utils;
import org.neo4j.driver.v1.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FlightModel extends Population {

    private final Logger logger = LoggerFactory.getLogger(FlightModel.class);

    private final int cityCount;
    private final int outgoingFlightsPerAirport;
    private final int flightsPerDestination;

    private Map<Integer, Airport> airports = new HashMap<>();
    private Map<Integer, City> cities = new HashMap<>();

    private DataFactory dataFactory = new DataFactory();

    private FlightModel(Builder builder) {
        this.cityCount = builder.cityCount;
        this.outgoingFlightsPerAirport = builder.outgoingFlightsPerAirport;
        this.flightsPerDestination = builder.flightsPerDestination;
    }

    public static class Builder {

        private int cityCount;
        private int outgoingFlightsPerAirport = 3;
        private int flightsPerDestination = 3;

        public Builder(int cityCount) {
            this.cityCount = cityCount;
        }

        public Builder outgoingFlightsPerAirport(int value){
            this.outgoingFlightsPerAirport = value;
            return this;
        }

        public Builder flightsPerDestination(int value) {
            this.flightsPerDestination = value;
            return this;
        }

        public FlightModel build(){
            return new FlightModel(this);
        }
    }

    @Override
    protected void generateNodesAndRelationships() {
        for (int i = 0; i < cityCount; i++) {
            // int cityId = createCity();
            // int airportId = createAirport(cities.get(cityId));
            // final TimeInterval t = new TimeInterval(TimeInterval.TIME_INTERVAL_MIN_YEAR, TimeInterval.TIME_INTERVAL_MAX_YEAR);
            // getHelper().createEdge(airportId, cityId, "LocatedAt", Collections.singletonList(t));
            // logger.debug("Completed approximately {}% of airport-city creations", (int) (100.0 * i / (cityCount - 1)));
        }

        airports.keySet().forEach(this::createFlights);
    }

    // private int createCity() {
    //     final TimeInterval t = new TimeInterval(TimeInterval.TIME_INTERVAL_MIN_YEAR, TimeInterval.TIME_INTERVAL_MAX_YEAR);
    //     final String timeInterval = t.toString();
    //     String name = dataFactory.getName(City.TITLE);
    //     final int cityId = createObject(City.TITLE, timeInterval);
    //     City city = new City(cityId, t);
    //     cities.put(cityId, city);
    //     return cityId;
    // }

    // private int createAirport(City city) {
    //     final TimeInterval t = new TimeInterval(TimeInterval.TIME_INTERVAL_MAX_YEAR);
    //     final String timeInterval = t.toString();
    //     final int airportId = createObject("Airport", timeInterval);
    //     Airport airport = new Airport(airportId, dataFactory.getName("Airport"), city, t);
    //     airports.put(airportId, airport);
    //     return airportId;
    // }

    private void createFlights(Integer airportId) {
        List<Record> destinations = getHelper().randomAirports(airportId, outgoingFlightsPerAirport);

        for (Record destination : destinations) {

            final int ID_INDEX = 0;
            final int destinationId = destination.get(ID_INDEX).asInt();

            final int flightStartOffset = Utils.randomInteger(0, 5);
            final LocalDateTime firstFlightDate = LocalDateTime.now().plusHours(flightStartOffset);
            final int minutesBetweenFlights = 30;

            List<DateTimeInterval> flightsTimes = new ArrayList<>(
                    DateTimeInterval.randomDateTimeIntervals(flightsPerDestination*2, firstFlightDate,
                            minutesBetweenFlights));

            List<DateTimeInterval> departureFlights = new ArrayList<>();
            List<DateTimeInterval> returnFlights = new ArrayList<>();

            for (int i = 0; i < flightsTimes.size(); i++) {
                DateTimeInterval flight = flightsTimes.get(i);
                if (i % 2 == 0) {
                    departureFlights.add(flight);
                } else {
                    returnFlights.add(flight);
                }
            }

            createFlightEdges(departureFlights, airportId, destinationId);
            createFlightEdges(returnFlights, destinationId, airportId);
        }
    }

    /**
     * Creates Flight edges between two airports
     * @param flightTimes list containing the departure times of the flights
     * @param originAirportId id of first airport
     * @param destinationAirportId id of second airport
     */
    private void createFlightEdges(List<DateTimeInterval> flightTimes, int originAirportId, int destinationAirportId) {
        List<String> flightNumbers = createFlightNumbers(flightTimes);
        getHelper().createFlightEdge(originAirportId, destinationAirportId, flightTimes, flightNumbers);
    }

    /**
     * Generates flight numbers for a list of flight times
     * @param dateTimeIntervals list of flight times
     * @return list of randomly generated flight numbers
     */
    private List<String> createFlightNumbers(List<DateTimeInterval> dateTimeIntervals) {
        return dateTimeIntervals.stream().map(f -> dataFactory.getFlightNumber()).collect(Collectors.toList());
    }
}
