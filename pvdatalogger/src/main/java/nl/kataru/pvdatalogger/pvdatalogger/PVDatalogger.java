package nl.kataru.pvdatalogger.pvdatalogger;

import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

/**
 * The main PVDatalogger class
 * 
 */
public class PVDatalogger {
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

		CommandLine commandLine = parseCommandLine(args);

		if (commandLine.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(0);
		}
		if (commandLine.hasOption("t")) {
			type = commandLine.getOptionValue("t");
			if (!("poller".equals(type) || "listener".equals(type))) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("pvdatalogger", options);

				System.exit(-1);
			}
		} else {
			HelpFormatter formatter = new HelpFormatter();
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
	private static void startPoller(CommandLine commandLine)
			throws InterruptedException {
		String inverter_address = "";
		int pollingInterval = DEFAULT_POLLING_INTERVAL;

		if (commandLine.hasOption("a")) {
			inverter_address = commandLine.getOptionValue("a");
		}
		if (commandLine.hasOption("i")) {
			pollingInterval = Integer.valueOf(commandLine.getOptionValue("i"));
		}

		OmnikDataPoller datalogger = new OmnikDataPoller(inverter_address);

		// TODO: Move while loop inside OmnikDataPoller which also uses Lambda
		// to print the results
		while (true) {
			Map<String, String> pvData = datalogger.retrieveData();

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
		}
		else {
			printRawData = false;
		}

		System.out.println("Listening on: " + listenerBindingAddress + ":"
				+ listenerPort);
		OmnikDataListener listener = new OmnikDataListener(
				listenerBindingAddress, listenerPort);
		listener.setTransformer(new OmnikDataTransformer());
		listener.run(data -> printData(data, printRawData));

		// Add a shutdown hook to gracefully shutdown when the application is
		// interupted, eg. crtl-c is pressed
		Runtime.getRuntime().addShutdownHook(
				new StopListenerShutdownHook(listener));
		while (true) {
		}

	}

	/**
	 * Output the data as a comma separated string to standard out
	 * 
	 * @param pvData
	 */
	private static void printData(Map<String, String> pvData,
			boolean printRawData) {
		System.out.print(pvData.get("timestamp"));
		System.out.print(",");
		System.out.print(pvData.get("serialnumber"));
		System.out.print(",");
		System.out.print(pvData.get("firmware_version_main"));
		System.out.print(",");
		System.out.print(pvData.get("firmware_version_slave"));
		System.out.print(",");
		System.out.print(pvData.get("inverter_model"));
		System.out.print(",");
		System.out.print(pvData.get("inverter_ratedpower"));
		System.out.print(",");
		System.out.print(pvData.get("yield_today"));
		System.out.print(",");
		System.out.print(pvData.get("yield_total"));
		System.out.print(",");
		System.out.print(pvData.get("hours_total"));
		System.out.print(",");
		System.out.print(pvData.get("temp"));
		System.out.print(",");
		System.out.print(pvData.get("vpv1"));
		System.out.print(",");
		System.out.print(pvData.get("vpv2"));
		System.out.print(",");
		System.out.print(pvData.get("vpv3"));
		System.out.print(",");
		System.out.print(pvData.get("ipv1"));
		System.out.print(",");
		System.out.print(pvData.get("ipv2"));
		System.out.print(",");
		System.out.print(pvData.get("ipv3"));
		System.out.print(",");
		System.out.print(pvData.get("iac1"));
		System.out.print(",");
		System.out.print(pvData.get("iac2"));
		System.out.print(",");
		System.out.print(pvData.get("iac3"));
		System.out.print(",");
		System.out.print(pvData.get("vac1"));
		System.out.print(",");
		System.out.print(pvData.get("vac2"));
		System.out.print(",");
		System.out.print(pvData.get("vac3"));
		System.out.print(",");
		System.out.print(pvData.get("fac1"));
		System.out.print(",");
		System.out.print(pvData.get("pac1"));
		System.out.print(",");
		System.out.print(pvData.get("fac2"));
		System.out.print(",");
		System.out.print(pvData.get("pac2"));
		System.out.print(",");
		System.out.print(pvData.get("fac3"));
		System.out.print(",");
		System.out.print(pvData.get("pac3"));
		System.out.print(",");
		System.out.print(stripNewlineCharacters(pvData.get("message")));
		System.out.print(",");
		System.out.print(pvData.get("alarms"));
		System.out.print(",");
		System.out.print(pvData.get("data_age"));
		if (printRawData) {
			System.out.print(",");
			System.out.print(pvData.get("rawdata"));
		}

		System.out.println();
	}

	/**
	 * @param input
	 * @return
	 */
	private static String stripNewlineCharacters(String input) {
		if (input == null)
			return null;

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

		Option option = OptionBuilder
				.withDescription("Print this help information.")
				.withLongOpt("help").create("h");
		options.addOption(option);

		option = OptionBuilder.withArgName("poller|listener").hasArg()
				.withDescription("The type of logger to use").isRequired()
				.withLongOpt("type").create("t");
		options.addOption(option);

		option = OptionBuilder.withArgName("ip or hostname").hasArg()
				.withDescription("The ip address or hostname of the inverter.")
				.withLongOpt("address").create("a");
		options.addOption(option);

		option = OptionBuilder
				.withArgName("minutes")
				.hasArg()
				.withDescription(
						"The polling interval in minutes. (Default is 5 minutes)")
				.withLongOpt("interval").create("i");
		options.addOption(option);

		option = OptionBuilder.withArgName("port").hasArg()
				.withDescription("The port to listen on").withLongOpt("port")
				.create("p");
		options.addOption(option);

		option = OptionBuilder
				.withArgName("address")
				.hasArg()
				.withDescription(
						"The address of the interface to bind the listener to. Use 0.0.0.0 to bind to all interfaces")
				.withLongOpt("bind").create("b");
		options.addOption(option);

		option = OptionBuilder.withDescription("Also print raw data as a hex string").withLongOpt("raw")
				.create("r");
		options.addOption(option);
		
		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(1);
		}

		return commandLine;
	}
}
