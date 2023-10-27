package ar.edu.itba.client;

import ar.edu.itba.population.Population;
import ar.edu.itba.population.PopulationModel;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Configuration {
    private static Configuration instance;

    private final static Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final String filename;
    private String neo4jUser = "neo4j";
    private String neo4jPassword = "password";
    private final PopulationModel populationModel;
    private Population population;
    private int webAppPort = 7000;
    private String boltHost = "localhost";
    private int boltPort = 7687;
    private String boltUri = "bolt://" + boltHost + ":" +  boltPort;

    private Configuration(CommandLineOptions commandLineOptions) {
        this.filename = commandLineOptions.getConfigurationFile();
        this.populationModel = commandLineOptions.getPopulationModel();

        if (this.filename == null) {
            logger.debug("Using default configuration");
            loadDefaults();
        } else if (new File(filename).isFile()) {
            logger.debug("Reading configuration file {}", filename);
            readConfigurationsFile();
        } else {
            logger.error("Could not read configuration file {}", this.filename);
            throw new IllegalArgumentException("Could not read configuration file.");
        }
        logger.info("Neo4J bolt uri: " + boltUri);
    }

    private void loadDefaults() {
        Configurations configs = new Configurations();
        try {
            population = parsePopulation(populationModel, configs.propertiesBuilder().getConfiguration());
        } catch (ConfigurationException e) {
            logger.error("Error parsing configuration file: {}", e.getMessage());
            System.exit(1);
        }
    }

    private void readConfigurationsFile() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(filename);
        PropertiesConfiguration config = configurationBuilder.build();

        boltHost = config.getString("database.bolt.host", boltHost);
        boltPort = config.getInt("database.bolt.port", boltPort);
        boltUri = "bolt://" + boltHost + ":" +  boltPort;
        neo4jUser = config.getString("database.username", neo4jUser);
        neo4jPassword = config.getString("database.password", neo4jPassword);
        webAppPort = config.getInt("webapp.port", webAppPort);
        population = parsePopulation(populationModel, config);
    }

    private Population parsePopulation(PopulationModel populationModel, PropertiesConfiguration configs) {
        Population population;

        switch (populationModel) {
            case SOCIAL_NETWORK:
                population = new SocialNetworkConfigurationParser().parse(configs);
                break;
            case FLIGHTS:
                population = new FlightsConfigurationParser().parse(configs);
                break;
            default:
                throw new IllegalStateException("Unexpected population: " + populationModel);
        }
        return population;
    }

    public static Configuration initialize (CommandLineOptions options) {
        if (instance == null) {
            instance = new Configuration(options);
        }
        return instance;
    }

    public static Configuration getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Configuration was not initialized");
        }
        return instance;
    }

    public String getBoltUri() {
        return boltUri;
    }

    public String getNeo4jUser() {
        return neo4jUser;
    }

    public String getNeo4jPassword() {
        return neo4jPassword;
    }

    public Population getPopulation() {
        return population;
    }

    public int getWebAppPort() {
        return webAppPort;
    }
}

