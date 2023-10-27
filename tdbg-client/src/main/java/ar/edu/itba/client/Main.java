package ar.edu.itba.client;

import ar.edu.itba.population.Population;
import ar.edu.itba.webapp.config.WebApp;

public class Main {

    public static void main(String[] args) {

        CommandLineOptions options = new CliParser().parseOptions(args);
        Configuration config = Configuration.initialize(options);
        
        if (options.isPopulateMode()) {
            Population p = config.getPopulation();
            p.create();
        } else {
            new WebApp(config.getWebAppPort()).start();
        }
    }
}
