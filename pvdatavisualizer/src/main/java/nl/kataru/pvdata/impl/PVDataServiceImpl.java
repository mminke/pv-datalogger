/**
 *
 */
package nl.kataru.pvdata.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import nl.kataru.pvdata.PVDataService;

/**
 * @author morten
 *
 */
@Stateless
public class PVDataServiceImpl implements PVDataService {

	@Inject
	MongoDatabase mongoDb;

	@Override
	public String getInverter(String id) {
		final MongoCollection<Document> dbCollection = mongoDb.getCollection("inverters");

		final Document query = new Document("serialnumber", id);

		final Document inverter = dbCollection.find(query).first();
		if (inverter == null) {
			return null;
		}

		return inverter.toJson();
	}

	@Override
	public void saveInverter(Document newInverter) {
		final MongoCollection<Document> dbCollection = mongoDb.getCollection("inverters");

		dbCollection.insertOne(newInverter);
	}

	@Override
	public String getActualData(String id) {
		final Document inverterData = getLastInverterData(id);

		if (inverterData == null) {
			return null;
		}

		final Document firstInverterDataForYear = getFirstInverterDataForYear(id, LocalDate.now().getYear());
		if (firstInverterDataForYear == null) {
			return null;
		}
		final Document firstInverterDataForYearTotals = (Document) firstInverterDataForYear.get("totals");

		// TODO: Add checks for existence
		final Document totals = (Document) inverterData.get("totals");
		final List groups = (List) inverterData.get("groups");
		final Document group1 = (Document) groups.get(0);
		final Document system = (Document) inverterData.get("system");

		final Document actualData = new Document();
		actualData.put("timestamp", inverterData.get("timestamp"));
		actualData.put("yield_std", totals.get("yield_total"));
		actualData.put("yield_ytd", (Double) totals.get("yield_total") - (Double) firstInverterDataForYearTotals.get("yield_total"));
		actualData.put("yield_today", totals.get("yield_today"));
		actualData.put("power", group1.get("pac"));
		actualData.put("voltage", group1.get("vpv"));
		actualData.put("temperature", system.get("temp"));

		return actualData.toJson();
	}

	/**
	 * TODO: Move this to a Data Layer service
	 *
	 * @param id
	 * @return
	 */
	private Document getLastInverterData(String id) {
		final MongoCollection<Document> dbCollection = mongoDb.getCollection("inverterdata");

		final Document query = new Document("inverter.serialnumber", id);
		final Document orderBy = new Document("timestamp", -1);

		final Document inverterData = dbCollection.find(query).sort(orderBy).first();

		return inverterData;
	}

	/**
	 * Retrieve the first inverter data record of the year specified.
	 * <br>
	 * sample mongodb query:
	 * db.inverterdata.findOne({$query: { "inverter.serialnumber": "NLDN2020145H2050", "timestamp": {"$gte": ISODate("2015-01-01T00:00:00.000Z")} }, $orderby: {"timestamp": 1} })
	 *
	 * @param id
	 * @param year
	 * @return
	 */
	private Document getFirstInverterDataForYear(String id, int year) {
		final MongoCollection<Document> dbCollection = mongoDb.getCollection("inverterdata");

		final Document query = new Document("inverter.serialnumber", id);
		query.append("timestamp", new Document("$gte", new Date(year - 1900, 0, 1)));
		// query.append("timestamp", new Document("$gte", LocalDate.now()));
		final Document orderBy = new Document("timestamp", 1);

		final Document inverterData = dbCollection.find(query).sort(orderBy).first();

		return inverterData;
	}

}
