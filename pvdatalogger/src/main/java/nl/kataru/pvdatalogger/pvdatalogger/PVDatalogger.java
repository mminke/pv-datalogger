package nl.kataru.pvdatalogger.pvdatalogger;

import org.apache.commons.cli.*;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The main PVDatalogger class
 *
 */
public class PVDatalogger {
	private static final Logger log = LoggerFactory.getLogger(PVDatalogger.class);

	private static final int DEFAULT_POLLING_INTERVAL = 5; // in minutes
	private static final int DEFAULT_LISTENER_PORT = 9999;
	private static final String DEFAULT_LISTENER_BINDING_ADDRESS = "0.0.0.0";
	private static Options options;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String type = "poller";

		// Enable Netty loggin
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		final CommandLine commandLine = parseCommandLine(args);

		if (commandLine.hasOption("h")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(0);
		}
		if (commandLine.hasOption("t")) {
			type = commandLine.getOptionValue("t");
			if (!("poller".equals(type) || "listener".equals(type))) {
				final HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("pvdatalogger", options);

				System.exit(-1);
			}
		} else {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(-1);
		}

		if ("poller".equals(type)) {
			startPoller(commandLine);
		} else {
			startListener(commandLine);
		}
	}

	/**
	 * Start the logger as a data poller
	 *
	 * @param commandLine
	 * @throws InterruptedException
	 */
	private static void startPoller(CommandLine commandLine) throws InterruptedException {
		String inverter_address = "";
		int pollingInterval = DEFAULT_POLLING_INTERVAL;

		if (commandLine.hasOption("a")) {
			inverter_address = commandLine.getOptionValue("a");
		}
		if (commandLine.hasOption("i")) {
			pollingInterval = Integer.valueOf(commandLine.getOptionValue("i"));
		}

		final OmnikDataPoller datalogger = new OmnikDataPoller(inverter_address);

		// TODO: Move while loop inside OmnikDataPoller which also uses Lambda
		// to print the results
		while (true) {
			final Map<String, String> pvData = datalogger.retrieveData();

			printData(pvData, false);

			Thread.sleep(pollingInterval * 60000);
		}
	}

	/**
	 * Start the logger as a listener
	 *
	 * @param commandLine
	 */
	private static void startListener(CommandLine commandLine) {
		int listenerPort = DEFAULT_LISTENER_PORT;
		final boolean printRawData;

		String listenerBindingAddress = DEFAULT_LISTENER_BINDING_ADDRESS;

		if (commandLine.hasOption("b")) {
			listenerBindingAddress = commandLine.getOptionValue("b");
		}
		if (commandLine.hasOption("p")) {
			listenerPort = Integer.valueOf(commandLine.getOptionValue("p"));
		}
		if (commandLine.hasOption("r")) {
			printRawData = true;
		} else {
			printRawData = false;
		}

		log.info("Listening on: " + listenerBindingAddress + ":" + listenerPort);
		final OmnikDataListener listener = new OmnikDataListener(listenerBindingAddress, listenerPort);
		listener.setTransformer(new OmnikDataTransformer());
		listener.run(data -> printData(data, printRawData));

		// Add a shutdown hook to gracefully shutdown when the application is
		// interupted, eg. crtl-c is pressed
		// TODO: Change OmnikDataListener to implement java.lang.AutoCloseable and replace the shutdown hook with a try-with-resource statement
		Runtime.getRuntime().addShutdownHook(new StopListenerShutdownHook(listener));
		while (true) {
		}

	}

	/**
	 * Output the data as a comma separated string to standard out
	 *
	 * @param pvData
	 */
	private static void printData(Map<String, String> pvData, boolean printRawData) {
		System.out.print(pvData.get("timestamp"));							// 1
		System.out.print(",");
		System.out.print(pvData.get("datalogger_sn"));                        // 2
		System.out.print(",");
		System.out.print(pvData.get("inverter_sn"));                        // 3
		System.out.print(",");
		System.out.print(pvData.get("firmware_version_main"));                // 4
		System.out.print(",");
		System.out.print(pvData.get("firmware_version_slave"));                // 5
		System.out.print(",");
		System.out.print(pvData.get("inverter_model"));                        // 6 <== only available when using the poller
		System.out.print(",");
		System.out.print(pvData.get("inverter_ratedpower"));                // 7 <== only available when using the poller
		System.out.print(",");
		System.out.print(pvData.get("yield_today"));                        // 8
		System.out.print(",");
		System.out.print(pvData.get("yield_total"));                        // 9
		System.out.print(",");
		System.out.print(pvData.get("hours_total"));                        // 10
		System.out.print(",");
		System.out.print(pvData.get("temp"));                                // 11
		System.out.print(",");
		System.out.print(pvData.get("vpv1"));                                // 12
		System.out.print(",");
		System.out.print(pvData.get("vpv2"));                                // 13
		System.out.print(",");
		System.out.print(pvData.get("vpv3"));                                // 14
		System.out.print(",");
		System.out.print(pvData.get("ipv1"));                                // 15
		System.out.print(",");
		System.out.print(pvData.get("ipv2"));                                // 16
		System.out.print(",");
		System.out.print(pvData.get("ipv3"));                                // 17
		System.out.print(",");
		System.out.print(pvData.get("iac1"));                                // 18
		System.out.print(",");
		System.out.print(pvData.get("iac2"));                                // 19
		System.out.print(",");
		System.out.print(pvData.get("iac3"));                                // 20
		System.out.print(",");
		System.out.print(pvData.get("vac1"));                                // 21
		System.out.print(",");
		System.out.print(pvData.get("vac2"));                                // 22
		System.out.print(",");
		System.out.print(pvData.get("vac3"));                                // 23
		System.out.print(",");
		System.out.print(pvData.get("fac1"));                                // 24
		System.out.print(",");
		System.out.print(pvData.get("pac1"));                                // 25
		System.out.print(",");
		System.out.print(pvData.get("fac2"));                                // 26
		System.out.print(",");
		System.out.print(pvData.get("pac2"));                                // 27
		System.out.print(",");
		System.out.print(pvData.get("fac3"));                                // 28
		System.out.print(",");
		System.out.print(pvData.get("pac3"));                                // 29
		System.out.print(",");
		System.out.print(stripNewlineCharacters(pvData.get("message")));    // 30
		System.out.print(",");
		System.out.print(pvData.get("alarms"));                                // 31 <== only available when using the poller
		System.out.print(",");
		System.out.print(pvData.get("data_age"));                            // 32 <== only available when using the poller
		if (printRawData) {
			System.out.print(",");
			System.out.print(pvData.get("rawdata"));                        // 33
		}

		System.out.println();
	}

	/**
	 * @param input
	 * @return
	 */
	private static String stripNewlineCharacters(String input) {
		if (input == null) {
			return null;
		}

		return input.replace("\n", "").replace("\r", "");
	}

	/**
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")
	private static CommandLine parseCommandLine(String[] args) {
		options = new Options();

		Option option = OptionBuilder.withDescription("Print this help information.").withLongOpt("help").create("h");
		options.addOption(option);

		option = OptionBuilder.withArgName("poller|listener").hasArg().withDescription("The type of logger to use").isRequired().withLongOpt("type").create("t");
		options.addOption(option);

		option = OptionBuilder.withArgName("ip or hostname").hasArg().withDescription("The ip address or hostname of the inverter.").withLongOpt("address").create("a");
		options.addOption(option);

		option = OptionBuilder.withArgName("minutes").hasArg().withDescription("The polling interval in minutes. (Default is 5 minutes)").withLongOpt("interval").create("i");
		options.addOption(option);

		option = OptionBuilder.withArgName("port").hasArg().withDescription("The port to listen on").withLongOpt("port").create("p");
		options.addOption(option);

		option = OptionBuilder.withArgName("address").hasArg().withDescription("The address of the interface to bind the listener to. Use 0.0.0.0 to bind to all interfaces").withLongOpt("bind").create("b");
		options.addOption(option);

		option = OptionBuilder.withDescription("Also print raw data as a hex string").withLongOpt("raw").create("r");
		options.addOption(option);

		final CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (final ParseException e) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(1);
		}

		return commandLine;
	}
}
