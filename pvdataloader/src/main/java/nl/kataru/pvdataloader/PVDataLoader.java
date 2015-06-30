package nl.kataru.pvdataloader;

import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.mongodb.DBObject;

/**
 * Main class which parses the program arguments, initializes a mongo db client and then listens to standard input for csv data.
 * Each line of csv data is transformed into a JSON object which is stored in the MongoDB database.
 */
public class PVDataLoader {
	private final PVDataRepository dataRepository;
	private static String databaseAddress = "localhost";
	private static int databasePort = 27017;
	private static String databaseName = "pvdata";
	private static final CSVDataTransformer transformer = new CSVDataTransformer();

	public static void main(String[] args) throws UnknownHostException {

		parseCommandLine(args);

		final PVDataLoader pvDataLoader = new PVDataLoader(databaseAddress, databasePort, databaseName);

		pvDataLoader.start();
	}

	private PVDataLoader(String databaseAddress, int databasePort, String databaseCollection) throws UnknownHostException {
		// Load config file

		// Initialise DataRepository
		dataRepository = new MongoPVDataRepository(databaseAddress, databasePort, databaseCollection);

		// Add a shutdown hook to gracefully shutdown when the application is
		// interupted, eg. crtl-c is pressed
		// TODO: Change PVDataRepository to implement java.lang.AutoCloseable
		// and replace the shutdown hook with a try-with-resource statement
		Runtime.getRuntime().addShutdownHook(new CleanShutdownHook(dataRepository));

	}

	private void start() {

		// Try with resource to force resource cleanup
		try (Scanner scanIn = new Scanner(System.in)) {
			while (true) {
				final String input = scanIn.nextLine();

				final DBObject pvData = transformer.transform(input);
				// Save the data
				dataRepository.save(pvData);

			}
		}
	}

	/**
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")
	private static void parseCommandLine(String[] args) {
		// Setup the expected command line options
		final Options options = new Options();

		Option option = OptionBuilder.withDescription("Print this help information.").withLongOpt("help").create("h");
		options.addOption(option);

		option = OptionBuilder.withArgName("address").hasArg().withDescription("The MongoDB database address (default is localhost).").withLongOpt("address").create("a");
		options.addOption(option);

		option = OptionBuilder.withArgName("port").hasArg().withDescription("The MongoDB database port (default is 27017).").withLongOpt("port").create("p");
		options.addOption(option);

		option = OptionBuilder.withArgName("database").hasArg().withDescription("The MongoDB database name in which all data is stored (default is pvdata).").withLongOpt("database").create("d");
		options.addOption(option);

		// Parse the command line
		final CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (final ParseException e) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(1);
		}

		// Determine values of arguments
		if (commandLine.hasOption("h")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(0);
		}
		if (commandLine.hasOption("a")) {
			databaseAddress = commandLine.getOptionValue("a");
		}
		if (commandLine.hasOption("p")) {
			try {
				databasePort = Integer.parseInt(commandLine.getOptionValue("p"));
			} catch (final RuntimeException exception) {
				System.out.println("Could not parse the database port. Please specify a correct number");
				System.exit(1);
			}
		}
		if (commandLine.hasOption("d")) {
			databaseName = commandLine.getOptionValue("d");
		}
	}

}
