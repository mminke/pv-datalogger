package nl.kataru.pvdataloader;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author morten
 *
 */
public class CSVDataTransformer {
	private final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

	/**
	 * Transform the given comma separated values (csv) input into a DBObject.
	 *
     * @param csv a single csv line.
     * @return a DBObject containing a structured representation of the given input data.
	 */
	public DBObject transform(String csv) {
		final String[] rawdata = csv.split(",");
        if (rawdata.length != 32 && rawdata.length != 33)
        {
            throw new TransformException("CSV input is not of the correct length. It should contain either 32 or 33 values.");
		}

		// Transform the rawdata into a storable object
		final DBObject pvData = new BasicDBObject();

		try {
			pvData.put("timestamp", isoDateFormat.parse(rawdata[0]));
		} catch (final ParseException e) {
			throw new TransformException("The first value is not a correct ISO date format. It cannot be converted to a Date object.");
		}

		try {
            final DBObject datalogger = new BasicDBObject();
            datalogger.put("serialnumber", rawdata[1]);
            pvData.put("datalogger", datalogger);

			final DBObject inverter = new BasicDBObject();
            inverter.put("serialnumber", rawdata[2]);
            inverter.put("firmware_version_main", rawdata[3]);
            inverter.put("firmware_version_slave", rawdata[4]);
			if (!rawdata[4].equals("null")) {
                inverter.put("model", rawdata[5]);
			}
			if (!rawdata[5].equals("null")) {
                inverter.put("ratedpower", rawdata[6]);
			}
			pvData.put("inverter", inverter);

			final DBObject totals = new BasicDBObject();
            totals.put("yield_today", Double.parseDouble(rawdata[7]));
            totals.put("yield_total", Double.parseDouble(rawdata[8]));
            totals.put("hours_total", Double.parseDouble(rawdata[9]));
			pvData.put("totals", totals);

			final BasicDBList groups = new BasicDBList();
            groups.add(createGroup(1, rawdata[11], rawdata[14], rawdata[17], rawdata[20], rawdata[23], rawdata[24]));
            groups.add(createGroup(2, rawdata[12], rawdata[15], rawdata[18], rawdata[21], rawdata[25], rawdata[26]));
            groups.add(createGroup(3, rawdata[13], rawdata[16], rawdata[19], rawdata[22], rawdata[27], rawdata[28]));
			pvData.put("groups", groups);

			final DBObject system = new BasicDBObject();
            system.put("temp", Double.parseDouble(rawdata[10]));
            system.put("message", rawdata[29]);
            if (!rawdata[30].equals("null"))
            {
                system.put("alarms", rawdata[30]);
			}
            if (!rawdata[31].equals("null"))
            {
                system.put("data_age", rawdata[31]);
			}
			pvData.put("system", system);

            if (rawdata.length == 33)
            {
                pvData.put("rawdata", rawdata[32]);
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
		group.put("vpv", Double.parseDouble(vpv));
		group.put("ipv", Double.parseDouble(ipv));
		group.put("iac", Double.parseDouble(iac));
		group.put("vac", Double.parseDouble(vac));
		group.put("fac", Double.parseDouble(fac));
		group.put("pac", Double.parseDouble(pac));

		return group;
	}

}
