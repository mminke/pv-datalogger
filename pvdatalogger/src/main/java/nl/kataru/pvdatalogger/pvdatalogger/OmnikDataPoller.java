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

class OmnikDataPoller {
	private String _inverterAddress;
	private static final DateFormat isoDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss SSS");


	public OmnikDataPoller(String inverterAddress) {
		_inverterAddress = inverterAddress;
	}

	public Map<String, String> retrieveData() {
		Map<String, String> pvData = new HashMap<String, String>();

		Date now = new Date();
		try {
			// Retrieve javascript file from the inverter
			Content content = Request
					.Get("http://" + _inverterAddress + "/js/status.js")
					.execute().returnContent();

			String rawContent = content.asString();

			// Extract data from raw data
			int index = rawContent.indexOf("webData=") + 9;
			rawContent = rawContent.substring(index);

			index = rawContent.indexOf("\"");
			rawContent = rawContent.substring(0, index);

			String rawData[] = rawContent.split(",");


			index = 0;
			pvData.put("timestamp", isoDateFormat.format(now));
			pvData.put("inverter_serialnumber", rawData[index++]);
			pvData.put("firmware_version_main", rawData[index++]);
			pvData.put("firmware_version_slave", rawData[index++]);
			pvData.put("inverter_model", rawData[index++]);
			pvData.put("inverter_ratedpower", rawData[index++]);
			pvData.put("current_power", rawData[index++]);
			pvData.put(
					"yield_today",
					BigDecimal.valueOf(Long.valueOf(rawData[index++]))
							.divide(new BigDecimal(100)).toString());
			pvData.put(
					"yield_total",
					BigDecimal.valueOf(Long.valueOf(rawData[index++]))
							.divide(new BigDecimal(10)).toString());
			pvData.put("alarms", rawData[index++]);
			pvData.put("data_age", rawData[index++]);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e instanceof NoRouteToHostException) {
				pvData.put("timestamp", isoDateFormat.format(now));
				pvData.put("inverter_serialnumber", "");
				pvData.put("firmware_version_main", "");
				pvData.put("firmware_version_slave", "");
				pvData.put("inverter_model", "");
				pvData.put("inverter_ratedpower", "");
				pvData.put("current_power", "");
				pvData.put(
						"yield_today",
						"");
				pvData.put(
						"yield_total",
						"");
				pvData.put("alarms", "connection fault[" + _inverterAddress + "]");
				pvData.put("data_age", "");

			} else {
				e.printStackTrace();
			}
		}

		return pvData;
	}

}