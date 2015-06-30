/**
 *
 */
package nl.kataru.pvdataloader;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

/**
 * @author morten
 *
 */
public class MongoPVDataRepository implements PVDataRepository {
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

		boolean success = false;
		while (!success) {
			try {
				collection.insert(object);
				success = true;
			} catch (final MongoException exception) {
				System.err.println("Error while inserting data in mongodb: " + exception.getMessage());
				try {
					Thread.sleep(30000); // Sleep for 30 seconds and try again
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
