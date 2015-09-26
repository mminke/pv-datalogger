package nl.kataru.pvdataloader;

import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import nl.kataru.pvdataloader.PVDataLoader.Mode;

/**
 * Main class which parses the program arguments, initializes the PVDataLoader and starts the process.
 */
public class PVDataLoaderApp {
	private static String databaseAddress = "localhost";
	private static int databasePort = 27017;
	private static String databaseName = "pvdata";
	private static Mode followOutput = Mode.READ_UNTIL_EOF;
	private static Path inputfilePath;

	public static void main(String[] args) throws UnknownHostException {

		parseCommandLine(args);

		final PVDataRepository dataRepository = new MongoPVDataRepository(databaseAddress, databasePort, databaseName);
		// Add a shutdown hook to gracefully shutdown when the application is
		// interupted, eg. crtl-c is pressed
		// TODO: Change PVDataRepository to implement java.lang.AutoCloseable
		// and replace the shutdown hook with a try-with-resource statement
		Runtime.getRuntime().addShutdownHook(new CleanShutdownHook(dataRepository));

		final PVDataLoader pvDataLoader = new PVDataLoader(inputfilePath, followOutput, dataRepository);

		pvDataLoader.start();

		System.out.println("Processed " + pvDataLoader.getLineCounter() + " lines.");
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

		option = OptionBuilder.withLongOpt("follow").withDescription("Keep following the input file for new data.").create("f");
		options.addOption(option);

		// Parse the command line
		final CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (final ParseException e) {
			exitWithUsageInfo(options, 1);
		}

		// Determine values of arguments
		if (commandLine.hasOption("h")) {
			exitWithUsageInfo(options, 0);
		}

		// Required command line options
		if (commandLine.hasOption("f")) {
			followOutput = Mode.FOLLOW_OUTPUT;
		}

		// Option command line options
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

		@SuppressWarnings("unchecked")
		final List<String> remainingArgs = commandLine.getArgList();
		if (remainingArgs.size() != 1) {
			exitWithUsageInfo(options, 1);
		}

		final String filename = remainingArgs.get(0);
		inputfilePath = Paths.get(filename);
		if (!Files.exists(inputfilePath)) {
			System.out.println("Input file does not exist: " + inputfilePath);

			System.exit(0);
		}

	}

	private static void exitWithUsageInfo(Options options, int exitCode) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar pvdataloader [OPTIONS] <FILENAME>", options);

		System.exit(exitCode);
	}

}
