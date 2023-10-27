package ar.edu.itba.client;

import ar.edu.itba.population.PopulationModel;
import org.apache.commons.cli.*;

public class CliParser {

    private String configurationFile = "client.properties";
    private boolean populate = false;
    private PopulationModel populationModel = PopulationModel.SOCIAL_NETWORK;

    private final static String HELP_OPTION = "h";
    private final static String POPULATE_OPTION = "p";
    private final static String CONFIGURATION_OPTION = "c";

    public CommandLineOptions parseOptions(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(HELP_OPTION)) {
                help(options);
            }

            if (cmd.hasOption(POPULATE_OPTION)) {
                populate = true;
                try {
                    populationModel = PopulationModel.fromString(cmd.getOptionValue(POPULATE_OPTION));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid population model");
                    throw new IllegalArgumentException();
                }
            }

            if (cmd.hasOption(CONFIGURATION_OPTION)) {
                configurationFile = cmd.getOptionValue(CONFIGURATION_OPTION);
            }

            return new CommandLineOptions(configurationFile, populate, populationModel);

        } catch (ParseException e) {
            System.err.println("Option not recognized or missing argument.");
            help(options);
            throw new IllegalArgumentException();
        }
    }

    private Options createOptions() {
        Options options = new Options();
        options.addOption(HELP_OPTION, "help", false, "Shows this screen.");
        options.addOption(POPULATE_OPTION, "populate", true, "Populate database.");
        options.addOption(CONFIGURATION_OPTION, "configuration", true, "Path to the configuration file.");
        return options;
    }

    private void help(Options options){
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("client", options);
    }
}
