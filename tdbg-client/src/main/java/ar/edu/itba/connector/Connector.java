package ar.edu.itba.connector;

import ar.edu.itba.client.Configuration;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connector {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private static Connector instance;
    private static Driver driver;

    private Connector() {
        logger.debug("Connecting to the database..");

        final Configuration configuration = Configuration.getInstance();
        try {
            driver = GraphDatabase.driver(configuration.getBoltUri(),
                    AuthTokens.basic(configuration.getNeo4jUser(), configuration.getNeo4jPassword()));
        } catch (ServiceUnavailableException e) {
            logger.error("Could not connect to database at {}", configuration.getBoltUri());
            return;
        }

        logger.debug("Connection established");
    }

    public static Connector getInstance() {
        if (instance == null || driver == null) {
            instance = new Connector();
        }
        return instance;
    }

    public Driver getDriver() {
        return driver;
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }
}
