/**
 *
 */
package nl.kataru.pvdataloader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author morten
 *
 */
public class CSVDataTransformer {
	private final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

	/**
	 * Transform the given comma separated values (csv) input into a DBObject.
	 *
	 * @param csv
	 * @return
	 */
	public DBObject transform(String csv) {
		final String[] rawdata = csv.split(",");
		if (rawdata.length != 31 && rawdata.length != 32) {
			throw new TransformException("CSV input is not of the correct length. It should contain either 31 or 32 values.");
		}

		// Transform the rawdata into a storable object
		final DBObject pvData = new BasicDBObject();

		try {
			pvData.put("timestamp", isoDateFormat.parse(rawdata[0]));
		} catch (final ParseException e) {
			throw new TransformException("The first value is not a correct ISO date format. It cannot be converted to a Date object.");
		}

		try {
			final DBObject inverter = new BasicDBObject();
			inverter.put("serialnumber", rawdata[1]);
			inverter.put("firmware_version_main", rawdata[2]);
			inverter.put("firmware_version_slave", rawdata[3]);
			if (!rawdata[4].equals("null")) {
				inverter.put("inverter_model", rawdata[4]);
			}
			if (!rawdata[5].equals("null")) {
				inverter.put("inverter_ratedpower", rawdata[5]);
			}
			pvData.put("inverter", inverter);

			final DBObject totals = new BasicDBObject();
			totals.put("yield_today", Float.parseFloat(rawdata[6]));
			totals.put("yield_total", Float.parseFloat(rawdata[7]));
			totals.put("hours_total", Float.parseFloat(rawdata[8]));
			pvData.put("totals", totals);

			final BasicDBList groups = new BasicDBList();
			groups.add(createGroup(1, rawdata[10], rawdata[13], rawdata[16], rawdata[19], rawdata[22], rawdata[23]));
			groups.add(createGroup(2, rawdata[11], rawdata[14], rawdata[17], rawdata[20], rawdata[24], rawdata[25]));
			groups.add(createGroup(3, rawdata[12], rawdata[15], rawdata[18], rawdata[21], rawdata[26], rawdata[27]));
			pvData.put("groups", groups);

			final DBObject system = new BasicDBObject();
			system.put("temp", Float.parseFloat(rawdata[9]));
			system.put("message", rawdata[28]);
			if (!rawdata[29].equals("null")) {
				system.put("alarms", rawdata[29]);
			}
			if (!rawdata[30].equals("null")) {
				system.put("data_age", rawdata[30]);
			}
			pvData.put("system", system);

			if (rawdata.length == 32) {
				pvData.put("rawdata", rawdata[31]);
			}
		} catch (final Exception exception) {
			throw new TransformException("Error occured while parsing the CSV data. Error message is: " + exception.getMessage());
		}

		return pvData;
	}

	/**
	 * Create a DBObject representing the values for a group of solarpanels connected to an inverter (also called a string)
	 *
	 * @param groupNumber
	 * @param vpv
	 * @param ipv
	 * @param iac
	 * @param vac
	 * @param fac
	 * @param pac
	 * @return
	 */
	private DBObject createGroup(int groupNumber, String vpv, String ipv, String iac, String vac, String fac, String pac) {
		final DBObject group = new BasicDBObject();
		group.put("groupNumber", groupNumber);
		group.put("vpv", Float.parseFloat(vpv));
		group.put("ipv", Float.parseFloat(ipv));
		group.put("iac", Float.parseFloat(iac));
		group.put("vac", Float.parseFloat(vac));
		group.put("fac", Float.parseFloat(fac));
		group.put("pac", Float.parseFloat(pac));

		return group;
	}

}
