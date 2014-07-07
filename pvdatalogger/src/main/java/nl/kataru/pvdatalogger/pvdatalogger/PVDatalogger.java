package nl.kataru.pvdatalogger.pvdatalogger;

import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Hello world!
 * 
 */
public class PVDatalogger {
	private static final int DEFAULT_LISTENER_PORT = 9999;

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option option = OptionBuilder.withArgName("ip or hostname").hasArg()
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

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = parser.parse(options, args);

		String address = "";
		int pollingInterval = 5; // In minutes

		if (commandLine.hasOption("a")) {
			address = commandLine.getOptionValue("a");
		} else {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pvdatalogger", options);

			System.exit(1);
		}
		if (commandLine.hasOption("p")) {
			pollingInterval = Integer.valueOf(commandLine.getOptionValue("p"));
		}

		OmnikDataPoller datalogger = new OmnikDataPoller(address);

		System.out.println("Starting Netty");
		new OmnikDataListener(DEFAULT_LISTENER_PORT).run();
		System.out.println("Netyy Run Ended"); 
		 
		while (true) {
			Map<String, String> pvData = datalogger.retrieveData();

			// Output the data
			System.out.print(pvData.get("timestamp") + ",");
			System.out.print(pvData.get("inverter_serialnumber") + ",");
			System.out.print(pvData.get("firmware_version_main") + ",");
			System.out.print(pvData.get("firmware_version_slave") + ",");
			System.out.print(pvData.get("inverter_model") + ",");
			System.out.print(pvData.get("inverter_ratedpower") + ",");
			System.out.print(pvData.get("current_power") + ",");
			System.out.print(pvData.get("yield_today") + ",");
			System.out.print(pvData.get("yield_total") + ",");
			System.out.print(pvData.get("alarms") + ",");
			System.out.print(pvData.get("data_age"));
			System.out.println();

			Thread.sleep(pollingInterval * 60000);
		}
	}
}
