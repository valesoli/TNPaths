package ar.edu.itba.population;

import ar.edu.itba.population.models.airports.Airport;
import ar.edu.itba.population.models.common.City;
import ar.edu.itba.population.models.socialnetwork.Brand;
import ar.edu.itba.population.models.socialnetwork.Person;
import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;

import java.util.Locale;

public class DataFactory {

    private Faker faker = new Faker();

    private FakeValuesService fakeValuesService = new FakeValuesService(
            new Locale("en-GB"), new RandomService());

    private String getFullName() {
        return faker.name().fullName();
    }

    private String getBrandName() {
        return faker.company().name();
    }

    private String getCityName() {
        return faker.address().cityName();
    }

    private String getAirportName() {
        return faker.name().fullName().concat(" Airport");
    }

    public String getFlightNumber() {
        return fakeValuesService.bothify("??####", true);
    }

    public String getName(String title) {
        switch (title){
            case Person.TITLE:
                return getFullName();
            case Brand.TITLE:
                return getBrandName();
            case City.TITLE:
                return getCityName();
            case Airport.TITLE:
                return getAirportName();
            default:
                throw new IllegalArgumentException("Title not supported");
        }
    }
}
