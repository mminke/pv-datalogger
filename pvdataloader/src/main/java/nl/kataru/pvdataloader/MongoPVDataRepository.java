/**
 *
 */
package nl.kataru.pvdataloader;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

/**
 * @author morten
 *
 */
public class MongoPVDataRepository implements PVDataRepository {
	private static final int RETRY_DELAY = 30000;	// Retry delay in milliseconds. If an insertion in the database fails, try again after this amount of time.
	private static final String COLLECTION = "inverterdata";
	private MongoClient mongoClient;
	private DB database;

	/**
	 * Initialize the Mongo DB client with the given address, port and database name.
	 *
	 * @param mongodb_address
	 * @param mongodb_port
	 * @param database_name
	 *
	 * @throws RuntimeException
	 *             in case the client cannot be initialized correctly.
	 */
	public MongoPVDataRepository(String mongodb_address, int mongodb_port, String database_name) {

		try {
			final MongoClientOptions options = MongoClientOptions.builder().build();
			mongoClient = new MongoClient(new ServerAddress(mongodb_address, mongodb_port), options);

			database = mongoClient.getDB(database_name);

			// create a unique index on the collection for the timestamp and serialnumber (each inverter should only log one item per time unit)
			final DBCollection collection = database.getCollection(COLLECTION);
			final DBObject index = new BasicDBObject().append("serialnumber", 1).append("timestamp", -1);
			final DBObject constraints = new BasicDBObject("unique", true);
			collection.createIndex(index, constraints);

		} catch (final UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Save the given object. If an error occurs while storing the object in MongoDB this method will retry
	 * indefinitely until it succeeds, or until the program is interrupted.
	 *
	 * @see nl.kataru.pvdataloader.PVDataRepository#save(java.lang.String)
	 */
	@Override
	public void save(DBObject object) {
		final DBCollection collection = database.getCollection(COLLECTION);

		boolean retry = true;
		while (retry) {
			try {
				collection.insert(object);
				retry = false;
			} catch (final DuplicateKeyException exception) {
				System.err.println("Warning, could not insert data, duplicate key exists: " + object);
				retry = false;
			} catch (final MongoException exception) {
				System.err.println("Error while inserting data in mongodb: " + exception.getMessage());
				System.err.println("Trying again in " + RETRY_DELAY + " ms.");
				try {
					Thread.sleep(RETRY_DELAY); // Sleep for 30 seconds and try again
				} catch (final InterruptedException interruptedException) {
					System.err.println("Error while sleeping: " + interruptedException.getMessage());
				}
			}
		}
	}

	/**
	 * Gracefully close the Mongo DB Client.
	 *
	 * @see nl.kataru.pvdataloader.PVDataRepository#close()
	 */
	@Override
	public void close() {
		mongoClient.close();
	}

}
