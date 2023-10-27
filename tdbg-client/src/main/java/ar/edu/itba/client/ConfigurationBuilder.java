package ar.edu.itba.client;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationBuilder {

    private final String filename;

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationBuilder.class);

    public ConfigurationBuilder(String filename) {
        this.filename = filename;
    }

    public PropertiesConfiguration build() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(filename)
                                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        try {
            return builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Error reading configuration file: {}", e.getMessage());
            System.exit(1);
        }

        return null;
    }

}
