package nl.kataru.pvdatalogger.pvdatalogger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.NoRouteToHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OmnikDataPoller {
	private static final Logger LOG = LoggerFactory.getLogger(OmnikDataPoller.class);

	private final String _inverterAddress;
	private final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

	public OmnikDataPoller(String inverterAddress) {
		_inverterAddress = inverterAddress;
	}

	public Map<String, String> retrieveData() {
		final Map<String, String> pvData = new HashMap<String, String>();

		final Date now = new Date();
		try {
			// Retrieve javascript file from the inverter
			final Content content = Request.Get("http://" + _inverterAddress + "/js/status.js").execute().returnContent();

			String rawContent = content.asString();

			// Extract data from raw data
			int index = rawContent.indexOf("webData=") + 9;
			rawContent = rawContent.substring(index);

			index = rawContent.indexOf("\"");
			rawContent = rawContent.substring(0, index);

			final String rawData[] = rawContent.split(",");

			index = 0;
			pvData.put("timestamp", isoDateFormat.format(now));
			pvData.put("serialnumber", rawData[index++]);
			pvData.put("firmware_version_main", rawData[index++]);
			pvData.put("firmware_version_slave", rawData[index++]);
			pvData.put("inverter_model", rawData[index++]);
			pvData.put("inverter_ratedpower", rawData[index++]);
			pvData.put("pac1", rawData[index++]);
			pvData.put("yield_today", BigDecimal.valueOf(Long.valueOf(rawData[index++])).divide(new BigDecimal(100)).toString());
			pvData.put("yield_total", BigDecimal.valueOf(Long.valueOf(rawData[index++])).divide(new BigDecimal(10)).toString());
			pvData.put("alarms", rawData[index++]);
			pvData.put("data_age", rawData[index++]);

		} catch (final ClientProtocolException exception) {
			LOG.error("Error retrieving data.", exception);
		} catch (final IOException exception) {
			if (exception instanceof NoRouteToHostException) {
				// The inverter can go offline if not enough power is available. Therefor if the host dissappears, this is not regarded as a fault.
				pvData.put("timestamp", isoDateFormat.format(now));
				pvData.put("serialnumber", "");
				pvData.put("firmware_version_main", "");
				pvData.put("firmware_version_slave", "");
				pvData.put("inverter_model", "");
				pvData.put("inverter_ratedpower", "");
				pvData.put("pac1", "");
				pvData.put("yield_today", "");
				pvData.put("yield_total", "");
				pvData.put("alarms", "connection fault[" + _inverterAddress + "]");
				pvData.put("data_age", "");

			} else {
				LOG.error("Error retrieving data.", exception);
			}
		}

		return pvData;
	}

}